package com.kitcha.file.service;

import com.kitcha.file.dto.FileCreateEvent;
import com.kitcha.file.entity.File;
import com.kitcha.file.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private S3Service s3Service;

    // PDF 바이트 배열
    private byte[] getPdfBytes(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }

    @Async("taskExecutor")
    public void createPdf(FileCreateEvent board) throws IOException {
        // PDF 문서 생성
        PDDocument document = new PDDocument();

        // 새로운 페이지 추가
        PDPage page = new PDPage();
        document.addPage(page);

        // 트루타입 폰트 로드
        InputStream titleFontStream = getClass().getResourceAsStream("/fonts/NotoSansKR-ExtraBold.ttf");
        PDType0Font titleFont = PDType0Font.load(document, titleFontStream);
        InputStream contentFontStream = getClass().getResourceAsStream("/fonts/NotoSansKR-Regular.ttf");
        PDType0Font contentFont = PDType0Font.load(document, contentFontStream);

        // 배너 이미지 로드
        InputStream inputStream = getClass().getResourceAsStream("/images/Background.png");
        PDImageXObject image = PDImageXObject.createFromByteArray(document, inputStream.readAllBytes(), "Background.png");

        // 이미지 크기 계산
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // 내용 문단 처리
        String content = board.getLongSummary();
        String[] lines = content.split("\n");
        float yPosition = 650;

        try {
            // 콘텐츠 스트림 생성
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // 배경 추가
            contentStream.drawImage(image, 0, 0, pageWidth, pageHeight);

            // 제목 추가 (기본 글꼴, 크기 18)
            contentStream.beginText();
            contentStream.setFont(titleFont, 18);
            contentStream.newLineAtOffset(50, pageHeight - 200); // x, y 좌표 (페이지에서 위치)

            float maxWidth = pageWidth - 100; // 좌우 여백 고려한 최대 폭
            List<String> wrappedLines = wrapText(board.getNewsTitle(), titleFont, 18, maxWidth);

            for (String line : wrappedLines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -26);
            }

            contentStream.endText();

            // 내용 추가 (기본 글꼴, 크기 12)
            float contentStartY = pageHeight - 200 - (wrappedLines.size() * 26) - 20;
            contentStream.beginText();
            contentStream.setFont(contentFont, 12);
            contentStream.newLineAtOffset(50, contentStartY);

            wrappedLines = wrapText(board.getLongSummary(), contentFont, 12, maxWidth);

            for (String line : wrappedLines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -20);
            }

            contentStream.endText();

            // 콘텐츠 스트림 닫기
            contentStream.close();

            // S3에 PDF 저장
            String s3Path = "kitcha/" + UUID.randomUUID().toString().replace("-", "") + ".pdf";
            byte[] bytes = getPdfBytes(document);
            s3Service.uploadFileToS3(s3Path, bytes);

            // DB에 PDF 메타데이터 저장
            File file = new File();
            file.setBoardId(board.getBoardId());
            file.setFileName(board.getNewsTitle());
            file.setFilePath(s3Path);
            fileRepository.save(file);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("FileService.makePdf() : PDF 파일 생성 오류");
        }
    }

    // 파일 Presigned URL 반환
    public String getPresignedFileUrl(Long boardId) {
        File file = fileRepository.findByBoardId(boardId);

        if (file == null) {
            return null;
        }
        Optional<String> url = s3Service.generatePresignedUrl(file.getFilePath(), file.getFileName());

        return url.orElse(null);
    }

    // 줄바꿈 계산 함수
    private List<String> wrapText(String text, PDFont font, int fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\n")) {
            StringBuilder currentLine = new StringBuilder();
            float currentWidth = 0;
            for (String word : paragraph.split(" ")) {
                float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
                if (currentWidth + wordWidth > maxWidth) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                    currentWidth = wordWidth;
                } else {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                        currentWidth += font.getStringWidth(" ") / 1000 * fontSize;
                    }
                    currentLine.append(word);
                    currentWidth += wordWidth;
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

}

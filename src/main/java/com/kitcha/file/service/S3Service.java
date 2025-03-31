package com.kitcha.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class S3Service {
    // AWS 버킷명 (환경변수로 관리)
    @Value("${AWS_S3_BUCKET}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /**
     * 주어진 S3 경로에 파일 업로드
     * @param s3Path S3에 저장될 객체의 키 (경로)
     * @param fileBytes 업로드할 파일의 바이트 배열
     */
    public void uploadFileToS3(String s3Path, byte[] fileBytes) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Path)
                .acl(ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
    }

    /**
     * 지정된 S3 객체 키와 파일명을 기반으로 Presigned URL 생성
     * 이 URL은 일정 시간 동안만 유효하며, 파일 다운로드에 사용됨
     *
     * @param key S3 객체 키 (파일 경로)
     * @param fileName 다운로드될 파일 이름 (확장자 제외)
     * @return 생성된 Presigned URL (옵셔널)
     */
    public Optional<String> generatePresignedUrl(String key, String fileName) {
        String encodedFileName = URLEncoder.encode(fileName + ".pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .responseContentDisposition("attachment; filename*=UTF-8''" + encodedFileName)
                .responseContentType("application/pdf")
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(3))
                .getObjectRequest(getObjectRequest)
                .build();

        URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();

        return Optional.of(presignedUrl.toString());
    }
}

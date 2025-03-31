package com.kitcha.file.consumer;

import com.kitcha.file.dto.FileCreateEvent;
import com.kitcha.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileEventConsumer {
    private final FileService fileService;
    private static final Logger log = LoggerFactory.getLogger(FileEventConsumer.class);

    @KafkaListener(topics = "create-pdf", groupId = "file-service-group-test")
    public void consume(Map<String, Object> data) throws IOException {
        log.info("Consumed data: {}", data);
        log.info("boardId class: {}", data.get("boardId").getClass());

        Long boardId = ((Number) data.get("boardId")).longValue();
        String newsTitle = data.get("newsTitle").toString();
        String longSummary = data.get("longSummary").toString();

        FileCreateEvent event = new FileCreateEvent(boardId, newsTitle, longSummary);
        fileService.createPdf(event);
    }
}

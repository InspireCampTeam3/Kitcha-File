package com.kitcha.file.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitcha.file.dto.Field;
import com.kitcha.file.dto.KafkaFileDto;
import com.kitcha.file.dto.Payload;
import com.kitcha.file.dto.Schema;
import com.kitcha.file.entity.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FileProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    List<Field> fields = Arrays.asList(
            new Field("int64", true, "file_id"),
            new Field("int64", true, "board_id"),
            new Field("string", true, "file_name"),
            new Field("string", true, "file_path")
    );

    Schema schema = Schema.builder()
            .type("struct")
            .fields(fields)
            .optional(false)
            .name("file")
            .build();

    @Autowired
    public FileProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public File send(String topic, File file) {
        Payload payload = Payload.builder()
                .file_id(file.getFileId())
                .board_id(file.getBoardId())
                .file_name(file.getFileName())
                .file_path(file.getFilePath())
                .build();

        KafkaFileDto kafkaBoardDto = new KafkaFileDto(schema, payload);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(kafkaBoardDto);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
        log.info("File sent to topic: " + topic);

        return file;
    }
}

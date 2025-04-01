package com.kitcha.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KafkaFileDto {
    private Schema schema;
    private Payload payload;
}

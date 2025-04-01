package com.kitcha.file.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Payload {
    private Long file_id;
    private Long board_id;
    private String file_name;
    private String file_path;
}

package com.kitcha.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileCreateEvent {
    private Long boardId;
    private String newsTitle;
    private String longSummary;
}

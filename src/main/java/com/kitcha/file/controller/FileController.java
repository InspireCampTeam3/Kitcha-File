package com.kitcha.file.controller;

import com.kitcha.file.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/apps/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping("/{boardId}/download")
    public String download(@PathVariable Long boardId,
                           HttpServletResponse response) throws IOException {

        return fileService.getPresignedFileUrl(boardId);
    }
}

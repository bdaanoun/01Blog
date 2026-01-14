package com.o1blog._blog.dto;

import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class PostRequest {
    
    private MultipartFile banner;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Content is required")
    private String content;
}

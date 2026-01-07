package com.o1blog._blog.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String banner;
    private String status;
    private LocalDateTime createdAt;
    private Long userId;
}

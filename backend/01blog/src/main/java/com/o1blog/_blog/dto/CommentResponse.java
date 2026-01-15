package com.o1blog._blog.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private String content;

    private Long authorId;
    private String authorName;

    private String createdAt;
}

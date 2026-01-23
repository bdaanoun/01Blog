package com.o1blog._blog.dto;

import java.time.LocalDateTime;

public record AdminReportedPostResponse(
        Long postId,
        String title,
        String reason,
        Long reporterId,
        String reporterUsername,
        Long authorId,
        String authorUsername,
        LocalDateTime createdAt,
        String status
) {}

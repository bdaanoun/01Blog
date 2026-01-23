package com.o1blog._blog.dto;

import java.time.LocalDateTime;

public record AdminReportedPostResponse(
        Long reportId,
        Long postId,
        String reason,
        Long authorId,
        String authorUsername,
        LocalDateTime reportedAt) {
}

package com.o1blog._blog.dto;

import java.time.LocalDateTime;

public record AdminReportedUsersResponse(
        Long reportId,
        Long reportedUserId,
        String reportedUsername,
        Long reporterId,
        String reporterUsername,
        String reason,
        LocalDateTime reportedAt) {
}

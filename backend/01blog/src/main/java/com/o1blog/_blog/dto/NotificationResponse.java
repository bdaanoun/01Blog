package com.o1blog._blog.dto;

import java.time.LocalDateTime;

import com.o1blog._blog.model.Notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private Long id;

    private Long senderId;
    private Long postId;
    private String senderUsername;

    private String content;

    private LocalDateTime createdAt;

    private Notification.NotificationStatus status;
    private Notification.NotificationType notifType;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .senderId(n.getSender().getId())
                .postId(n.getPostId())
                .senderUsername(n.getSender().getUsername())
                .content(n.getContent())
                .createdAt(n.getCreatedAt())
                .status(n.getStatus())
                .notifType(n.getNotifType())
                .build();
    }
}

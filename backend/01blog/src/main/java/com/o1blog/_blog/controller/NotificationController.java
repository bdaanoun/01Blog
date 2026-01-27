package com.o1blog._blog.controller;

import com.o1blog._blog.dto.NotificationResponse;
import com.o1blog._blog.model.Notification;
import com.o1blog._blog.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // Get my notifications (newest first)
    @GetMapping
    public List<NotificationResponse> myNotifications() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null)
            throw new RuntimeException("User not authenticated");

        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    // Count unread
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null)
            throw new RuntimeException("User not authenticated");

        long count = notificationRepository.countByReceiverIdAndStatus(
                currentUserId, Notification.NotificationStatus.UNREAD);
        return ResponseEntity.ok(count);
    }

    // Mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null)
            throw new RuntimeException("User not authenticated");

        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!n.getReceiver().getId().equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        n.setStatus(Notification.NotificationStatus.READ);
        notificationRepository.save(n);
        return ResponseEntity.noContent().build();
    }

    // Mark one as unread
    @PatchMapping("/{id}/unread")
    public ResponseEntity<Void> markAsUnRead(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null)
            throw new RuntimeException("User not authenticated");

        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!n.getReceiver().getId().equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        n.setStatus(Notification.NotificationStatus.UNREAD);
        notificationRepository.save(n);
        return ResponseEntity.noContent().build();
    }

    // Mark all as read
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null)
            throw new RuntimeException("User not authenticated");

        List<Notification> unread = notificationRepository
                .findByReceiverIdAndStatus(currentUserId, Notification.NotificationStatus.UNREAD);

        unread.forEach(n -> n.setStatus(Notification.NotificationStatus.READ));
        notificationRepository.saveAll(unread);

        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return null;
        var principal = auth.getPrincipal();
        if (principal instanceof com.o1blog._blog.security.CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}

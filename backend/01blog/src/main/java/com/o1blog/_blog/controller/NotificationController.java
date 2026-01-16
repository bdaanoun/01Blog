package com.o1blog._blog.controller;

import com.o1blog._blog.model.Notification;
import com.o1blog._blog.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<List<Notification>> myNotifications(@RequestParam Long myUserId) {
        List<Notification> notifs = notificationRepository
                .findByReceiverId(myUserId, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(notifs);
    }

    // Count unread
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@RequestParam Long myUserId) {
        long count = notificationRepository.countByReceiverIdAndStatus(
                myUserId, Notification.NotificationStatus.UNREAD
        );
        return ResponseEntity.ok(count);
    }

    // Mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam Long myUserId) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // basic ownership check
        if (!n.getReceiver().getId().equals(myUserId)) {
            return ResponseEntity.status(403).build();
        }

        n.setStatus(Notification.NotificationStatus.READ);
        notificationRepository.save(n);
        return ResponseEntity.noContent().build();
    }

    // Mark all as read
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long myUserId) {
        List<Notification> unread = notificationRepository
                .findByReceiverIdAndStatus(myUserId, Notification.NotificationStatus.UNREAD);

        unread.forEach(n -> n.setStatus(Notification.NotificationStatus.READ));
        notificationRepository.saveAll(unread);

        return ResponseEntity.noContent().build();
    }
}

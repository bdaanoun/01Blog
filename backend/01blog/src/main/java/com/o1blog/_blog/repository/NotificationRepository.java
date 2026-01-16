package com.o1blog._blog.repository;

import com.o1blog._blog.model.Notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    long countByReceiverIdAndStatus(Long receiverId, Notification.NotificationStatus status);

    List<Notification> findByReceiverIdAndStatus(Long receiverId, Notification.NotificationStatus status);
}

package com.o1blog._blog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "reported_at", updatable = false)
    private LocalDateTime reportedAt;

    @PrePersist
    protected void onReport() {
        this.reportedAt = LocalDateTime.now();
    }
}

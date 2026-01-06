package com.o1blog._blog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data                   // getters, setters, toString, equals, hashCode
@NoArgsConstructor      // empty constructor
@AllArgsConstructor     // constructor with all fields
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String avatar;

    @Column(length = 500)
    private String bio;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Role role;     // e.g. USER, ADMIN

    @Column(nullable = false)
    private Status status;   // e.g. ACTIVE, BANNED

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        USER, ADMIN
    }

    public enum Status {
        ACTIVE, BANNED
    }
}

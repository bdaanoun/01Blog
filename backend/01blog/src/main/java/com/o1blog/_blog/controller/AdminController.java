package com.o1blog._blog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.o1blog._blog.dto.AdminReportedPostResponse;
import com.o1blog._blog.dto.AdminReportedUsersResponse;
import com.o1blog._blog.dto.UsersAdminResponse;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.service.AdminService;
import com.o1blog._blog.service.PostService;
import com.o1blog._blog.service.ReportService;
import com.o1blog._blog.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor

public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final AdminService adminService;
    private final ReportService reportService;

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        userService.deleteUser(id, currentUsername);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public List<UsersAdminResponse> getAllUsers() {
        return userService.getAllUsersForAdmin();
    }

    @GetMapping("/reported-users")
    public List<AdminReportedUsersResponse> getReportedUsers() {
        return adminService.getReportedUsersForAdmin();
    }

    @GetMapping("/reported-posts")
    public List<AdminReportedPostResponse> getReportedPosts() {
        return adminService.getReportedPostsForAdmin();
    }

    @PatchMapping("/posts/{id}/hide")
    public void hidePost(@PathVariable Long id) {
        postService.hidePost(id);
    }

    @PatchMapping("/posts/{id}/show")
    public void showPost(@PathVariable Long id) {
        postService.showPost(id);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id, getCurrentUserId());
    }

    @DeleteMapping("reports/{id}")
    public void deleteReport(@PathVariable Long id) {
        reportService.deletePostReport(id);
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

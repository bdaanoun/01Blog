package com.o1blog._blog.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
// import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.o1blog._blog.dto.AdminReportedPostResponse;
import com.o1blog._blog.dto.AdminReportedUsersResponse;
import com.o1blog._blog.dto.UsersAdminResponse;
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

    // public AdminController(UserService userService, PostService postService,
    // AdminService adminService) {
    // this.userService = userService;
    // this.postService = postService;
    // this.adminService = adminService;
    // }

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
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/users")
    public List<UsersAdminResponse> getAllUsers() {
        return userService.getAllUsersForAdmin();
    }

    @GetMapping("/reported-users")
    public List<AdminReportedUsersResponse> getReportedUsers() {
        // Long currentUserId = getCurrentUserId();
        return adminService.getReportedUsersForAdmin();
    }

    @GetMapping("/reported-posts")
    public List<AdminReportedPostResponse> getReportedPosts() {
        return adminService.getReportedPostsForAdmin();
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    @DeleteMapping("reports/{id}")
    public void deleteReport(@PathVariable Long id) {
        System.out.println("dakhl---> " + id);
        reportService.deletePostReport(id);
    }

    // private Long getCurrentUserId() {
    // var auth =
    // org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    // if (auth == null || !auth.isAuthenticated())
    // return null;
    // var principal = auth.getPrincipal();
    // if (principal instanceof com.o1blog._blog.security.CustomUserDetails
    // userDetails) {
    // return userDetails.getId();
    // }
    // return null;
    // }
}

package com.o1blog._blog.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.o1blog._blog.dto.AdminReportedPostResponse;
import com.o1blog._blog.dto.UserProfileResponse;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.repository.PostReportRepository;
import com.o1blog._blog.service.PostService;
import com.o1blog._blog.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final PostReportRepository postReportRepository;

    public AdminController(UserService userService, PostService postService,
            PostReportRepository postReportRepository) {
        this.userService = userService;
        this.postService = postService;
        this.postReportRepository = postReportRepository;
    }

    @GetMapping("/users")
    public List<UserProfileResponse> getAllUsers() {
        Long currentUserId = getCurrentUserId();
        return userService.getAllUsersWithFollowStatus(currentUserId);
    }

    @GetMapping("/posts")
    public List<AdminReportedPostResponse> getAllPosts() {
        return postReportRepository.findAdminReportedPosts();
    }

    @GetMapping("/reported-posts")
    public List<Post> getReportedPosts() {
        return postService.getAllPosts();
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
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

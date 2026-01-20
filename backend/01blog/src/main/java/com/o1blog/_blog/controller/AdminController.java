package com.o1blog._blog.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.o1blog._blog.dto.UserProfileResponse;
import com.o1blog._blog.model.User;
import com.o1blog._blog.service.PostService;
import com.o1blog._blog.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    // private final PostService postService;

    public AdminController(UserService userService/* , PostService postService */) {
        this.userService = userService;
        // this.postService = postService;
    }

    @GetMapping("/users")
    public List<UserProfileResponse> getAllUsers() {
        Long currentUserId = getCurrentUserId();
        return userService.getAllUsersWithFollowStatus(currentUserId);
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

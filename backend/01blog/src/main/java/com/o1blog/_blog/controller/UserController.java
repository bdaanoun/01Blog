package com.o1blog._blog.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.o1blog._blog.dto.PostResponse;
import com.o1blog._blog.model.User;
import com.o1blog._blog.service.PostService;
import com.o1blog._blog.service.UserService;

import lombok.Data;

@RestController
@Data
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.creatUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/posts")
    public List<PostResponse> getUserPosts(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        return postService.getPostsByUser(id, currentUserId);
    }

    @PostMapping("/{id}/follow")
    public void followUser(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }
        userService.toggleFollow(currentUserId, id);
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

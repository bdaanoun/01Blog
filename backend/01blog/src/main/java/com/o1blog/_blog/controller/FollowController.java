package com.o1blog._blog.controller;

import com.o1blog._blog.dto.FollowResponse;
import com.o1blog._blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FollowController {

    private final UserService userService;

    @PostMapping("/follow/{id}")
    public ResponseEntity<FollowResponse> toggleFollow(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }
        FollowResponse response = userService.toggleFollow(currentUserId, id);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        var principal = auth.getPrincipal();
        if (principal instanceof com.o1blog._blog.security.CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}

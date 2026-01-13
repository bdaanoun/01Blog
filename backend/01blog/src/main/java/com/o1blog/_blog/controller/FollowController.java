package com.o1blog._blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.o1blog._blog.service.FollowService;

import lombok.Data;

@RestController
@Data
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    // public FollowController(FollowService followService) {
    //     this.followService = followService;
    // }

    @PostMapping("/{followingId}")
    public ResponseEntity<String> followUser(@PathVariable Long followingId, @RequestParam Long followerId) {
        followService.followUser(followerId, followingId);
        return ResponseEntity.ok("Followed successfully");
    }
}

package com.o1blog._blog.controller;

import com.o1blog._blog.dto.ReportPostRequest;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.PostReport;
import com.o1blog._blog.model.User;
import com.o1blog._blog.model.UserReport;
import com.o1blog._blog.repository.PostReportRepository;
import com.o1blog._blog.repository.PostRepository;
import com.o1blog._blog.repository.UserReportRepository;
import com.o1blog._blog.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;
    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportPostRequest req) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Post not found"));
        }

        PostReport report = PostReport.builder()
                .post(post)
                .reason(req.getReason().trim())
                .build();

        postReportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Report sent successfully"));
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<?> reportUser(@PathVariable Long userId, @Valid @RequestBody ReportPostRequest req) {

        Long reporterId = getCurrentUserId();
        if (reporterId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        User reporter = userRepository.findById(reporterId).orElse(null);
        if (reporter == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        User reportedUser = userRepository.findById(userId).orElse(null);
        if (reportedUser == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reason(req.getReason().trim())
                .build();

        userReportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "User reported successfully"));
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return null;

        var principal = auth.getPrincipal();
        if (principal instanceof com.o1blog._blog.security.CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;

    }
}

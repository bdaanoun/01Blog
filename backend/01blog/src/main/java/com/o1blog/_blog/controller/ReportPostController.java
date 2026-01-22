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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportPostController {

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
    public ResponseEntity<?> reportUser(
            @PathVariable Long userId,
            @Valid @RequestBody ReportPostRequest req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        UserReport report = UserReport.builder()
                .reportedUser(user)
                .reason(req.getReason().trim())
                .build();

        userReportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "User reported successfully"));
    }
}

package com.o1blog._blog.controller;

import com.o1blog._blog.dto.CommentRequest;
import com.o1blog._blog.dto.CommentResponse;
import com.o1blog._blog.model.User;
import com.o1blog._blog.security.CustomUserDetails;
import com.o1blog._blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        // System.out.println("===> " + request);
        Long userId = user.getId();
        return ResponseEntity.ok(commentService.addComment(postId, request, userId));
    }
}

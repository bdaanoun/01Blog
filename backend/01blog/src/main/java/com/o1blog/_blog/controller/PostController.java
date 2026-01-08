package com.o1blog._blog.controller;

import com.o1blog._blog.dto.PostRequest;
import com.o1blog._blog.dto.PostResponse;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // CREATE POST (Rich Text)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal User user,
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "banner", required = false) MultipartFile banner) {
        Post post = postService.createPost(user, title, content, banner);
        return ResponseEntity.ok(mapToResponse(post));
    }

    // GET ALL POSTS
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        return ResponseEntity.ok(
                postService.getAllPosts()
                        .stream()
                        .map(this::mapToResponse)
                        .toList());
    }

    // GET SINGLE POST
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        Post post = postService.getPostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return ResponseEntity.ok(mapToResponse(post));
    }

    // DELETE POST
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // Mapper
    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent()) // HTML here
                .banner(post.getBanner())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser().getId())
                .build();
    }
}

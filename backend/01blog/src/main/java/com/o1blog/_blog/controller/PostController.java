package com.o1blog._blog.controller;

import com.o1blog._blog.dto.PostRequest;
import com.o1blog._blog.dto.PostResponse;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.security.CustomUserDetails;
import com.o1blog._blog.service.FileStorageService;
import com.o1blog._blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;

    // Upload image temporarily (before post is saved)
    @PostMapping("/images/temp")
    public ResponseEntity<Map<String, Object>> uploadTempImage(
            @RequestParam("image") MultipartFile image) {

        try {
            System.out.println("Uploading temp image: " + image.getOriginalFilename());
            String imagePath = fileStorageService.saveTemp(image);
            System.out.println("Temp image saved: " + imagePath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", 1);
            response.put("file", Map.of(
                    "url", "http://localhost:8080/uploads/temp/" + imagePath));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error uploading temp image: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", 0);
            errorResponse.put("message", "Upload failed: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // CREATE POST
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "banner", required = false) MultipartFile banner) {

        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

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
                .content(post.getContent())
                .banner(post.getBanner())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser().getId())
                .build();
    }
}

package com.o1blog._blog.controller;

import com.o1blog._blog.dto.PostResponse;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.repository.LikeRepository;
import com.o1blog._blog.security.CustomUserDetails;
import com.o1blog._blog.service.FileStorageService;
import com.o1blog._blog.service.PostService;

import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;

    // Upload image temporarily
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

    @PostMapping("/video/temp")
    public ResponseEntity<Map<String, Object>> uploadTempVideo(
            @RequestParam("video") MultipartFile video) {

        try {
            System.out.println("Uploading temp image: " + video.getOriginalFilename());
            String videoPath = fileStorageService.saveTemp(video);
            System.out.println("Temp video saved: " + videoPath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", 1);
            response.put("file", Map.of(
                    "url", "http://localhost:8080/uploads/temp/" + videoPath));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error uploading temp video: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", 0);
            errorResponse.put("message", "Upload failed: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // CREATE POST
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(@RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "banner", required = false) MultipartFile banner) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Post post = postService.createPost(user, title, content, banner);

        return ResponseEntity.ok(mapToResponse(post, user.getId()));
    }

    // GET ALL POSTS
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        Long currentUserId = getCurrentUserId();

        return ResponseEntity.ok(
                postService.getAllPosts(currentUserId)
                        .stream()
                        .map(post -> mapToResponse(post, currentUserId))
                        .toList());
    }

    // GET SINGLE POST
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();

        Post post = postService.getPostById(id, currentUserId);
        return ResponseEntity.ok(mapToResponse(post, currentUserId));
    }

    // ediit post
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "banner", required = false) MultipartFile banner) {
        Long currentUserId = getCurrentUserId();
        Post updated = postService.updatePost(id, currentUserId, title, content, banner);

        long likesCount = likeRepository.countByPostId(updated.getId());
        boolean liked = likeRepository.existsByPostIdAndUserId(updated.getId(), currentUserId);

        return ResponseEntity.ok(PostResponse.from(updated, liked, likesCount));
    }

    // GET FOLLOWING POST
    @GetMapping("/following")
    public ResponseEntity<List<PostResponse>> getFollowingPosts() {
        Long currentUserId = getCurrentUserId();
        // System.out.println("id: " + currentUserId);

        List<Post> posts = postService.getFollowingPosts(currentUserId);
        // System.out.println("posts: " + posts);

        List<PostResponse> response = posts.stream()
                .map(post -> mapToResponse(post, currentUserId))
                .toList();
        // System.out.println("response: " + response);

        return ResponseEntity.ok(response);
    }

    // LIKE/UNLIKE POST
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        boolean isLiked = postService.toggleLike(id, user.getId());
        long likesCount = likeRepository.countByPostId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", isLiked);
        response.put("likesCount", likesCount);

        return ResponseEntity.ok(response);
    }

    // DELETE POST
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    // Mapper
    private PostResponse mapToResponse(Post post, Long currentUserId) {
        long likesCount = likeRepository.countByPostId(post.getId());
        boolean likedByCurrentUser = currentUserId != null &&
                likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .banner(post.getBanner())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser().getId())
                .authorName(post.getUser().getUsername())
                .authorAvatar(post.getUser().getAvatar())
                .likesCount(likesCount)
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

    private Long getCurrentUserId() {
        try {
            CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
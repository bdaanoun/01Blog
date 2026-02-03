package com.o1blog._blog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.o1blog._blog.dto.PostResponse;
import com.o1blog._blog.exeption.PostNotFoundException;
import com.o1blog._blog.exeption.UserNotFoundException;
import com.o1blog._blog.model.Follow;
import com.o1blog._blog.model.Like;
import com.o1blog._blog.model.Notification;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.model.Post.PostStatus;
import com.o1blog._blog.repository.FollowRepository;
import com.o1blog._blog.repository.LikeRepository;
import com.o1blog._blog.repository.NotificationRepository;
import com.o1blog._blog.repository.PostRepository;
import com.o1blog._blog.repository.UserRepository;
import com.o1blog._blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Post createPost(CustomUserDetails userDetails, String title, String content, MultipartFile banner) {
        try {
            System.out.println("=== CREATE POST START ===");
            // System.out.println("User ID: " + userDetails.getId());
            // System.out.println("Title: " + title);
            // System.out.println("Content length: " + (content != null ? content.length() :
            // "null"));

            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Process EditorJS content to move temp images to permanent storage
            String processedContent = processEditorJSImages(content);

            String bannerPath = null;
            if (banner != null && !banner.isEmpty()) {
                System.out.println("Saving banner...");
                bannerPath = fileStorageService.save(banner);
                System.out.println("Banner saved: " + bannerPath);
            }

            Post post = Post.builder()
                    .user(user)
                    .title(title)
                    .content(processedContent)
                    .banner(bannerPath)
                    .build();
            Post savedPost = postRepository.save(post);

            // Notify followers that this user created a post
            List<Follow> followers = followRepository.findAllByFollowing(user); // people who follow "user"
            if (!followers.isEmpty()) {
                List<Notification> notifs = followers.stream()
                        .map((Follow f) -> Notification.builder()
                                .sender(user)
                                .receiver(f.getFollower())
                                .postId(savedPost.getId())
                                .content(user.getUsername() + " created a new post")
                                .notifType(Notification.NotificationType.NEW_POST)
                                .status(Notification.NotificationStatus.UNREAD)
                                .build())
                        .collect(Collectors.toList());

                notificationRepository.saveAll(notifs);
            }
            return savedPost;

        } catch (Exception e) {
            // System.err.println("=== ERROR IN CREATE POST ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create post: " + e.getMessage(), e);
        }
    }

    public List<Post> getFollowingPosts(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Follow> follows = followRepository.findAllByFollower(currentUser);

        List<User> followedUsers = follows.stream()
                .map(Follow::getFollowing)
                .toList();

        if (followedUsers.isEmpty())
            return List.of();

        if (isAdmin(currentUserId)) {
            return postRepository.findAllByUserIn(followedUsers);
        }

        return postRepository.findAllByUserInAndStatus(followedUsers, PostStatus.PUBLISHED);
    }

    private boolean isAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return user.getRole() == User.Role.ADMIN;
    }

    private String processEditorJSImages(String content) {
        try {

            if (content == null || content.trim().isEmpty()) {
                System.out.println("Content is empty");
                return content;
            }

            JsonNode root = objectMapper.readTree(content);
            System.out.println("JSON parsed successfully");

            JsonNode blocks = root.get("blocks");
            if (blocks == null || !blocks.isArray()) {
                System.out.println("No blocks array found");
                return content;
            }

            // System.out.println("Processing " + blocks.size() + " blocks");

            for (int i = 0; i < blocks.size(); i++) {
                JsonNode block = blocks.get(i);

                if (block.has("type") && "image".equals(block.get("type").asText())) {
                    JsonNode data = block.get("data");
                    if (data != null && data.has("file")) {
                        JsonNode fileNode = data.get("file");
                        if (fileNode.has("url")) {
                            String url = fileNode.get("url").asText();
                            System.out.println("Found image URL: " + url);

                            if (url.contains("/temp/")) {
                                String filename = url.substring(url.lastIndexOf("/") + 1);
                                System.out.println("Moving temp file: " + filename);

                                String newPath = fileStorageService.moveTempToPermanent(filename);
                                String newUrl = "http://localhost:8080/uploads/" + newPath;

                                ((ObjectNode) fileNode).put("url", newUrl);
                                System.out.println("Updated to: " + newUrl);
                            }
                        }
                    }
                }
                if (block.has("type") && "video".equals(block.get("type").asText())) {
                    JsonNode data = block.get("data");
                    if (data != null && data.has("url")) {
                        String url = data.get("url").asText();
                        System.out.println("Found video URL: " + url);

                        if (url.contains("/temp/")) {
                            String filename = url.substring(url.lastIndexOf("/") + 1);
                            System.out.println("Moving temp video file: " + filename);

                            String newPath = fileStorageService.moveTempToPermanent(filename);
                            System.out.println("New video path: " + newPath);
                            String newUrl = "http://localhost:8080/uploads/" + newPath;

                            ((ObjectNode) data).put("url", newUrl);
                            System.out.println("Updated video to: " + newUrl);
                        }
                    }
                }

            }

            String result = objectMapper.writeValueAsString(root);
            System.out.println("=== PROCESS IMAGES END ===");
            return result;

        } catch (Exception e) {
            System.err.println("=== ERROR IN PROCESS IMAGES ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            // Return original content if processing fails
            return content;
        }
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            // Unlike
            likeRepository.delete(existingLike.get());
            return false;
        } else {
            // Like
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            return true;
        }
    }

    @Transactional
    public Post updatePost(Long postId, Long currentUserId, String title, String content, MultipartFile banner) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to edit this post");
        }

        if (title != null && !title.isBlank())
            post.setTitle(title);
        if (banner != null && !banner.isEmpty()) {
            String bannerPath = fileStorageService.save(banner);
            post.setBanner(bannerPath);
        }
        if (content != null && !content.isBlank())
            post.setContent(processEditorJSImages(content));

        return postRepository.save(post);
    }

    // get all posts
    public List<Post> getAllPosts(Long currentUserId) {
        if (isAdmin(currentUserId)) {
            return postRepository.findAll();
        }
        return postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED);
    }

    // get one user posts
    public List<PostResponse> getPostsByUser(Long profileUserId, Long currentUserId) {
        List<Post> posts = postRepository.findByUserId(profileUserId);

        return posts.stream().map(post -> {
            boolean liked = likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
            return PostResponse.from(post, liked, currentUserId);
        }).toList();
    }

    public Post getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (post.getStatus() == PostStatus.PUBLISHED)
            return post;

        if (isAdmin(currentUserId) || post.getUser().getId().equals(currentUserId))
            return post;

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
    }

    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own post");
        }

        postRepository.delete(post);
    }

    public void hidePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setStatus(Post.PostStatus.HIDDEN);
        postRepository.save(post);
    }

    public void showPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setStatus(Post.PostStatus.PUBLISHED);
        postRepository.save(post);
    }

    public Post updatePost(Post post) {
        return postRepository.save(post);
    }
}
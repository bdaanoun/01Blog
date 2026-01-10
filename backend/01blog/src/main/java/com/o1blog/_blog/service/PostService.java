package com.o1blog._blog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.o1blog._blog.model.Like;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.LikeRepository;
import com.o1blog._blog.repository.PostRepository;
import com.o1blog._blog.repository.UserRepository;
import com.o1blog._blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Post createPost(CustomUserDetails userDetails, String title, String content, MultipartFile banner) {
        try {
            System.out.println("=== CREATE POST START ===");
            System.out.println("User ID: " + userDetails.getId());
            System.out.println("Title: " + title);
            System.out.println("Content length: " + (content != null ? content.length() : "null"));
            
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

            System.out.println("Saving post to database...");
            Post savedPost = postRepository.save(post);
            System.out.println("Post saved successfully with ID: " + savedPost.getId());
            System.out.println("=== CREATE POST END ===");
            
            return savedPost;
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN CREATE POST ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create post: " + e.getMessage(), e);
        }
    }

    private String processEditorJSImages(String content) {
        try {
            System.out.println("=== PROCESS IMAGES START ===");
            
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
            
            System.out.println("Processing " + blocks.size() + " blocks");
            
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

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Post updatePost(Post post) {
        return postRepository.save(post);
    }
}
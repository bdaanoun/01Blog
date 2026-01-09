package com.o1blog._blog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.PostRepository;
import com.o1blog._blog.repository.UserRepository;
import com.o1blog._blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper; // ✅ THIS WAS MISSING

    public Post createPost(CustomUserDetails userDetails,
                           String title,
                           String content,
                           MultipartFile banner) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Process EditorJS images
        String processedContent = processEditorJSImages(content);

        String bannerPath = null;
        if (banner != null && !banner.isEmpty()) {
            bannerPath = fileStorageService.save(banner);
        }

        Post post = Post.builder()
                .user(user)
                .title(title)
                .content(processedContent)
                .banner(bannerPath)
                .build();

        return postRepository.save(post);
    }

    private String processEditorJSImages(String content) {

        if (content == null || content.isBlank()) {
            return content;
        }

        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode blocks = root.get("blocks");

            if (blocks == null || !blocks.isArray()) {
                return content;
            }

            for (JsonNode block : blocks) {

                if (!"image".equals(block.path("type").asText())) {
                    continue;
                }

                JsonNode data = block.path("data");
                JsonNode fileNode = data.path("file");

                if (fileNode.isMissingNode() || !fileNode.has("url")) {
                    continue;
                }

                String url = fileNode.get("url").asText();

                if (!url.contains("/uploads/temp/")) {
                    continue;
                }

                String filename = url.substring(url.lastIndexOf("/") + 1);

                String newPath = fileStorageService.moveTempToPermanent(filename);
                String newUrl = "http://localhost:8080/uploads/" + newPath;

                // ✅ SAFE JSON mutation
                ((ObjectNode) data).set(
                        "file",
                        objectMapper.createObjectNode().put("url", newUrl)
                );
            }

            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process images", e);
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

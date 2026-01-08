package com.o1blog._blog.service;

// import com.o1blog._blog.dto.PostRequest;
import com.o1blog._blog.model.Post;
// import com.o1blog._blog.model.Post.PostStatus;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;

// import org.jsoup.Jsoup;
// import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;

    public Post createPost(User user, String title, String content, MultipartFile banner) {
        System.out.println("user:  " + user);
        System.out.println("title:  " + title);

        String bannerPath = null;

        if (banner != null && !banner.isEmpty()) {
            bannerPath = fileStorageService.save(banner);
        }

        Post post = Post.builder()
                .user(user)
                .title(title)
                .content(content) // EditorJS JSON string
                .banner(bannerPath)
                .build();

        System.out.println("post:  " + post);

        return postRepository.save(post);
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

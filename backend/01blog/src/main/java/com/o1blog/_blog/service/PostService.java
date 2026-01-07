package com.o1blog._blog.service;

import com.o1blog._blog.dto.PostRequest;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.Post.PostStatus;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post createPost(User user, PostRequest request) {

        String safeContent = Jsoup.clean(
                request.getContent(),
                Safelist.basic());

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(safeContent)
                .banner(request.getBanner())
                .status(PostStatus.PUBLISHED)
                .build();

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

package com.o1blog._blog.service;

import com.o1blog._blog.dto.CommentRequest;
import com.o1blog._blog.dto.CommentResponse;
import com.o1blog._blog.model.Comment;
import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.CommentRepository;
import com.o1blog._blog.repository.PostRepository;
import com.o1blog._blog.repository.UserRepository;
// import com.o1blog._blog.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return commentRepository.findByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CommentResponse addComment(Long postId, CommentRequest request, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .build();
        Comment saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getUser().getId())
                .authorName(comment.getUser().getUsername())
                .avatar(comment.getUser().getAvatar())
                .createdAt(comment.getCreatedAt().toString())
                .build();
    }
}

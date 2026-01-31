package com.o1blog._blog.repository;

import com.o1blog._blog.model.Post;
import com.o1blog._blog.model.Post.PostStatus;
import com.o1blog._blog.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    List<Post> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);

    List<Post> findAllByUserIn(List<User> authors);

    // added
    List<Post> findByUserIdAndStatus(Long userId, PostStatus status);

    List<Post> findByUserIdInAndStatusOrderByCreatedAtDesc(List<Long> userIds, PostStatus status);

    List<Post> findAllByUserInAndStatus(List<User> authors, PostStatus status);

    List<Post> findAllByStatusOrderByCreatedAtDesc(PostStatus status);

}

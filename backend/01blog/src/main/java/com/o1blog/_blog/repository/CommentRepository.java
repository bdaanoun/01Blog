package com.o1blog._blog.repository;

import com.o1blog._blog.model.Comment;
import com.o1blog._blog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
}

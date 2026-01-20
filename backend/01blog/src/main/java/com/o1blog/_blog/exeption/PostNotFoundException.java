package com.o1blog._blog.exeption;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("Post not found: " + id);
    }
}

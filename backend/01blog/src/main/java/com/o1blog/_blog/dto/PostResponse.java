package com.o1blog._blog.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.o1blog._blog.model.Post;

@Data
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String banner;
    private String status;
    private LocalDateTime createdAt;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long likesCount;
    private Boolean likedByCurrentUser;

    public static PostResponse from(Post post, boolean likedByCurrentUser) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .banner(post.getBanner())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser().getId())
                .authorName(post.getUser().getUsername())
                // .authorAvatar("post.getUser().getAvatar()")
                .likesCount(post.getId())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

}

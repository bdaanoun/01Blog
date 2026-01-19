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

    public static PostResponse from(Post post, boolean likedByCurrentUser, long likesCount) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .banner(post.getBanner())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser().getId())
                .authorName(post.getUser().getUsername())
                .authorAvatar(post.getUser().getAvatar())
                .likesCount(likesCount)
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

}

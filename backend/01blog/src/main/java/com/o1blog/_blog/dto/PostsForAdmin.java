package com.o1blog._blog.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostsForAdmin {
    Long id;
    String title;
    Long userId;
    String authorUsername;
    String status;
}

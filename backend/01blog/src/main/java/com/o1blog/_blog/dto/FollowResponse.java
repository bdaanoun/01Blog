package com.o1blog._blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponse {
    private Boolean isFollowing;
    private Integer followersCount;
}

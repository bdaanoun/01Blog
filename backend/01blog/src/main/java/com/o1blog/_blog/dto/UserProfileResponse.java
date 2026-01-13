package com.o1blog._blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String avatar;
    private String username;
    private String email;
    private String bio;
    private Integer followersCount;
    private Integer followingCount;
    private Boolean isFollowing;
}

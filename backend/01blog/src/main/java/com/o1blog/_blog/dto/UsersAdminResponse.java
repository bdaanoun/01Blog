package com.o1blog._blog.dto;


import com.o1blog._blog.model.User.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UsersAdminResponse {
    private Long id;
    private String avatar;
    private Role role;
    private String username;
    private String email;
    private String status;

}

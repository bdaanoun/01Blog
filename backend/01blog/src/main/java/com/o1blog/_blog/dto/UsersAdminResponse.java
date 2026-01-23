package com.o1blog._blog.dto;


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
    private String username;
    private String email;
    private String status;

}

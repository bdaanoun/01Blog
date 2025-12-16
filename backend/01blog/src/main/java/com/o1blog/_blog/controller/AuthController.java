package com.o1blog._blog.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.o1blog._blog.dto.AuthResponse;
import com.o1blog._blog.dto.LoginRequest;
import com.o1blog._blog.model.User;
// import com.o1blog._blog.service.AuthService;
import com.o1blog._blog.service.AuthService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return authService.register(user);
    }
}

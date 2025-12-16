package com.o1blog._blog.service;

import com.o1blog._blog.dto.AuthResponse;
import com.o1blog._blog.dto.LoginRequest;
import com.o1blog._blog.dto.RegisterRequest;
import com.o1blog._blog.model.User;
// import com.o1blog._blog.entity.User;
import com.o1blog._blog.repository.UserRepository;
import com.o1blog._blog.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(null, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, "Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        // Generate token using email (since your LoginRequest uses email)
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(token, "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Authenticate user using email instead of username
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            // Generate token
            String token = jwtUtil.generateToken(user.getEmail());

            return new AuthResponse(token, "Login successful");

        } catch (Exception e) {
            return new AuthResponse(null, "Invalid email or password");
        }
    }
}
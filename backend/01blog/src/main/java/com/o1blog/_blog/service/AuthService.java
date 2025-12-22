package com.o1blog._blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.o1blog._blog.dto.AuthResponse;
import com.o1blog._blog.dto.LoginRequest;
import com.o1blog._blog.dto.RegisterRequest;
import com.o1blog._blog.model.User;
// import com.o1blog._blog.entity.User;
import com.o1blog._blog.repository.UserRepository;
import com.o1blog._blog.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // @Autowired
    // private AuthenticationManager authenticationManager;

    public ResponseEntity<AuthResponse> register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Username already exists"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Email already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(token, "Registration successful"));
    }

    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getIdentifier())
                    .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                            .orElseThrow(() -> new IllegalArgumentException("User makaynch.")));

            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(
                    new AuthResponse(token, "Login successful"));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(null, "Invalid email or password"));
        }
    }
}

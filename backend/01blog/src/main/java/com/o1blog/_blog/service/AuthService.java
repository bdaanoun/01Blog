package com.o1blog._blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.o1blog._blog.dto.AuthResponse;
import com.o1blog._blog.dto.LoginRequest;
// import com.o1blog._blog.dto.RegisterRequest;
import com.o1blog._blog.model.User;
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
    @Autowired
    private FileStorageService fileStorageService;

    public ResponseEntity<AuthResponse> register(
            String username,
            String email,
            String password,
            String bio,
            MultipartFile avatar) {

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Username already exists"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Email already exists"));
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setBio(bio == null ? "" : bio);
        user.setPassword(passwordEncoder.encode(password));

        if (avatar != null && !avatar.isEmpty()) {
            System.out.println("----filename-----");

            // try {
            String filename = fileStorageService.saveAvatar(avatar);
            user.setAvatar(filename);
            System.out.println("----filename-----" + filename);

            // } catch (IOException e) {
            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            // .body(new AuthResponse(null, "Failed to upload avatar"));
            // }
        }
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, "Registration successful"));
    }

    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getIdentifier())
                    .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                            .orElseThrow(() -> new IllegalArgumentException("User makaynch.")));

            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password");
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            return ResponseEntity.ok(
                    new AuthResponse(token, "Login successful"));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(null, "Invalid " + e));
        }
    }
}

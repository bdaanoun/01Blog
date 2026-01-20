package com.o1blog._blog.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.UserRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminUsername.isBlank() || adminPassword.isBlank())
            return;

        // only create if no admin exists
        if (userRepository.existsByRole(User.Role.ADMIN))
            return;

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.ADMIN);

        userRepository.save(admin);

        System.out.println("Default admin created: " + adminEmail);
    }
}

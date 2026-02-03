package com.o1blog._blog.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.UserRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Default admin credentials
    private static final String DEFAULT_EMAIL = "admin@bilal.com";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    public AdminSeeder(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // If an admin already exists - do nothing
        if (userRepository.existsByRole(User.Role.ADMIN)) {
            System.out.println("Admin already exists. Skipping seeding.");
            return;
        }

        User admin = new User();
        admin.setEmail(DEFAULT_EMAIL);
        admin.setUsername(DEFAULT_USERNAME);
        admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        admin.setRole(User.Role.ADMIN);

        userRepository.save(admin);

    }
}

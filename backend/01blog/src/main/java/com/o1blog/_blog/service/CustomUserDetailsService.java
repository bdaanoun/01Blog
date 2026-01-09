package com.o1blog._blog.service;

import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.UserRepository;
import com.o1blog._blog.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // identifier can be either email or username
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with identifier: " + identifier)));

        return new CustomUserDetails(user);
    }
}

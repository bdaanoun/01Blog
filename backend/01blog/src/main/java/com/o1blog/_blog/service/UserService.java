package com.o1blog._blog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.UserRepository;

public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User creatUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

package com.o1blog._blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.o1blog._blog.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}

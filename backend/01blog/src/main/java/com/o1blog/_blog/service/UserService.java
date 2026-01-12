package com.o1blog._blog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.o1blog._blog.model.Follow;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.FollowRepository;
import com.o1blog._blog.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // public UserService(UserRepository userRepository) {
    // this.userRepository = userRepository;
    // }

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

    public void toggleFollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId))
            return; // can't follow yourself

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .ifPresentOrElse(follow -> {
                    // already following, so unfollow
                    followRepository.delete(follow);
                }, () -> {
                    // not following yet, so follow
                    Follow newFollow = Follow.builder()
                            .follower(currentUser)
                            .following(targetUser)
                            .build();
                    followRepository.save(newFollow);
                });
    }

}

package com.o1blog._blog.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.o1blog._blog.dto.FollowResponse;
import com.o1blog._blog.dto.UserProfileResponse;
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

    public List<UserProfileResponse> getAllUsersWithFollowStatus(Long currentUserId) {
        List<User> users = userRepository.findAll();

        List<UserProfileResponse> userProfiles = users.stream()
                .map(user -> {
                    // Check if current user is following this user
                    boolean isFollowing = false;
                    if (currentUserId != null && !currentUserId.equals(user.getId())) {
                        User currentUser = userRepository.findById(currentUserId).orElse(null);
                        if (currentUser != null) {
                            isFollowing = followRepository.findByFollowerAndFollowing(currentUser, user).isPresent();
                        }
                    }

                    // Get followers and following counts
                    int followersCount = followRepository.countByFollowingId(user.getId());
                    int followingCount = followRepository.countByFollowerId(user.getId());

                    return UserProfileResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .bio(user.getBio())
                            .avatar(user.getAvatar())
                            .followersCount(followersCount)
                            .followingCount(followingCount)
                            .isFollowing(isFollowing)
                            .build();
                })
                .collect(Collectors.toList());

        return userProfiles;
    }

    public UserProfileResponse getUserProfile(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if current user is following this user
        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(userId)) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                isFollowing = followRepository.findByFollowerAndFollowing(currentUser, user).isPresent();
            }
        }

        // Get followers and following counts
        int followersCount = followRepository.countByFollowingId(userId);
        int followingCount = followRepository.countByFollowerId(userId);

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatar(user.getAvatar())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public FollowResponse toggleFollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        boolean isFollowing;

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

        // Check the current state after toggle
        isFollowing = followRepository.findByFollowerAndFollowing(currentUser, targetUser).isPresent();

        // Get the updated follower count for the target user
        int followersCount = followRepository.countByFollowingId(targetUserId);

        return new FollowResponse(isFollowing, followersCount);
    }
}

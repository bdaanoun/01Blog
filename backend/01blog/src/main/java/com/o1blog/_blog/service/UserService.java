package com.o1blog._blog.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.o1blog._blog.dto.FollowResponse;
import com.o1blog._blog.dto.UserProfileResponse;
import com.o1blog._blog.dto.UsersAdminResponse;
import com.o1blog._blog.exeption.EmailAlreadyTakenException;
import com.o1blog._blog.exeption.UserNotFoundException;
import com.o1blog._blog.exeption.UsernameAlreadyTakenException;
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
    private final FileStorageService fileStorageService;

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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
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

    @Transactional(readOnly = true)
    public List<UsersAdminResponse> getAllUsersForAdmin() {
        return userRepository.findAll()
                .stream()
                .map(u -> UsersAdminResponse.builder()
                        .id(u.getId())
                        .avatar(u.getAvatar())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .status(u.getStatus().toString())
                        .build())
                .toList();
    }

    public UserProfileResponse getUserProfile(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
                .role(user.getRole() != null ? user.getRole().name() : null)
                .avatar(user.getAvatar())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long id, Long currentUserId,
            String username, String email, String bio, MultipartFile avatar) {

        if (!id.equals(currentUserId))
            throw new RuntimeException("You can only edit your own profile");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (username != null && !username.isBlank() && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new UsernameAlreadyTakenException();
            }
            user.setUsername(username);
        }

        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyTakenException();
            }
            user.setEmail(email);
        }

        if (bio != null)
            user.setBio(bio);

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = fileStorageService.saveAvatar(avatar);
            user.setAvatar(avatarUrl);
        }

        userRepository.save(user);
        return getUserProfile(id, currentUserId);
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

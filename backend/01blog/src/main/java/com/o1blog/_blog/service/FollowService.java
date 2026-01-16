package com.o1blog._blog.service;

import org.springframework.stereotype.Service;

import com.o1blog._blog.model.Follow;
import com.o1blog._blog.model.Notification;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.FollowRepository;
import com.o1blog._blog.repository.NotificationRepository;
import com.o1blog._blog.repository.UserRepository;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public FollowService(FollowRepository followRepository,
                         UserRepository userRepository,
                         NotificationRepository notificationRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public Follow followUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        Follow saved = followRepository.save(follow);

        //Create notification for the followed user
        
        Notification notif = Notification.builder()
                .sender(follower)
                .receiver(following)
                .content(follower.getUsername() + " started following you")
                .NotifType(Notification.NotificationType.FOLLOW)
                .status(Notification.NotificationStatus.UNREAD)
                .build();

        notificationRepository.save(notif);

        return saved;
    }
}

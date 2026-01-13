package com.o1blog._blog.repository;

import com.o1blog._blog.model.Follow;
import com.o1blog._blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    int countByFollowerId(Long followerId);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    int countByFollowingId(Long followingId);

    void deleteByFollowerAndFollowing(User follower, User following);

    List<Follow> findAllByFollower(User follower);
}

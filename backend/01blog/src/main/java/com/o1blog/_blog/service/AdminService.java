package com.o1blog._blog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.o1blog._blog.dto.AdminReportedPostResponse;
import com.o1blog._blog.dto.AdminReportedUsersResponse;
import com.o1blog._blog.model.User;
import com.o1blog._blog.repository.PostReportRepository;
import com.o1blog._blog.repository.UserReportRepository;
import com.o1blog._blog.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

        private final PostReportRepository postReportRepository;
        private final UserReportRepository userReportRepository;
        private final UserRepository userRepository;

        public List<AdminReportedPostResponse> getReportedPostsForAdmin() {
                return postReportRepository.findAllByOrderByReportedAtDesc()
                                .stream()
                                .map(r -> new AdminReportedPostResponse(
                                                r.getId(),
                                                r.getPost().getId(),
                                                r.getReason(),
                                                r.getPost().getUser().getId(),
                                                r.getPost().getUser().getUsername(),
                                                r.getReportedAt()))
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<AdminReportedUsersResponse> getReportedUsersForAdmin() {
                return userReportRepository.findAllByOrderByReportedAtDesc()
                                .stream()
                                .map(r -> new AdminReportedUsersResponse(
                                                r.getId(),
                                                r.getReportedUser().getId(),
                                                r.getReportedUser().getUsername(),
                                                r.getReporter().getId(),
                                                r.getReporter().getUsername(),
                                                r.getReason(),
                                                // r.getStatus(),
                                                r.getReportedAt()))
                                .toList();
        }

        @Transactional
        public void banUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                user.setStatus(User.Status.BANNED);
                userRepository.save(user);
        }

        @Transactional
        public void unbanUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                user.setStatus(User.Status.ACTIVE);
                userRepository.save(user);
        }

}
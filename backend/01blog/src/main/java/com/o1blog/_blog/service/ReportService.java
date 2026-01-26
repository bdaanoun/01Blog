package com.o1blog._blog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.o1blog._blog.repository.PostReportRepository;
import com.o1blog._blog.repository.UserReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PostReportRepository postReportRepo;
    private final UserReportRepository userReportRepo;

    @Transactional
    public void deletePostReport(Long id) {
        postReportRepo.deleteById(id);
    }

    @Transactional
    public void deleteUserReport(Long id) {
        userReportRepo.deleteById(id);
    }
}

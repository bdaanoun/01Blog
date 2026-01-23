package com.o1blog._blog.repository;

import com.o1blog._blog.dto.AdminReportedUsersResponse;
import com.o1blog._blog.model.UserReport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findAllByOrderByReportedAtDesc();
}

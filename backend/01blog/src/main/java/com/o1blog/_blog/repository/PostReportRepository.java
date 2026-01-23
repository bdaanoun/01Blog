package com.o1blog._blog.repository;

import com.o1blog._blog.model.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    List<PostReport> findAllByOrderByReportedAtDesc();

    // @Query("""
    // select new com.o1blog._blog.dto.AdminReportedPostResponse(
    // r.id,
    // p.id,
    // p.title,
    // r.reason,
    // author.id,
    // author.username,
    // r.reportedAt
    // )
    // from PostReport r
    // join r.post p
    // join p.user author
    // order by r.reportedAt desc
    // """)
    // List<AdminReportedPostResponse> findAdminReportedPosts();
}

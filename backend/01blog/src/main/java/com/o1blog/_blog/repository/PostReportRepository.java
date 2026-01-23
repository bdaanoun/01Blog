package com.o1blog._blog.repository;

import com.o1blog._blog.dto.AdminReportedPostResponse;
import com.o1blog._blog.model.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    @Query("""
                select new com.o1blog._blog.dto.AdminReportedPostResponse(
                    r.id,
                    p.id,
                    p.title,
                    r.reason,
                    reporter.id,
                    reporter.username,
                    author.id,
                    author.username,
                    r.createdAt,
                    r.status
                )
                from PostReport r
                join r.post p
                join r.reporter reporter
                join p.user author
                order by r.createdAt desc
            """)
    List<AdminReportedPostResponse> findAdminReportedPosts();
}

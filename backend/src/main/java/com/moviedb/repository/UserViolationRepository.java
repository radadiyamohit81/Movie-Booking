package com.moviedb.repository;

import com.moviedb.model.UserViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for user-submitted violation reports.
 *
 * Named queries cover the two primary admin use cases:
 *  - List all pending reports (moderation queue)
 *  - List reports by a specific user (abuse detection)
 */
@Repository
public interface UserViolationRepository extends JpaRepository<UserViolation, Long> {

    /** All reports against a specific piece of content. */
    List<UserViolation> findByContentTypeAndContentId(String contentType, Long contentId);

    /** All reports submitted by a given user (detect malicious reporters). */
    List<UserViolation> findByReporterId(Long reporterId);

    /** Pending moderation queue — sorted oldest first for FIFO processing. */
    List<UserViolation> findByStatusOrderByCreatedAtAsc(String status);

    /** Count unresolved reports to display in admin dashboard. */
    long countByStatus(String status);
}

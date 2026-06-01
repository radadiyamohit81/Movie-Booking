package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records a user-submitted violation report against a piece of content
 * (a Review, CustomList, or another User).
 *
 * WHY a dedicated table?
 *   Violation reports are a separate moderation workflow from flagging reviews
 *   directly (ContentModerationService). A UserViolation captures who reported,
 *   what was reported, the reason, and the resolution status — none of which
 *   belong on the Review/User entity itself.
 *
 * WHY contentType as a String?
 *   The target can be any content type ("REVIEW", "LIST", "USER").
 *   A String is more flexible than an enum during the early stages of the
 *   moderation feature; convert to an enum when the type set is stable.
 */
@Entity
@Table(name = "user_violations",
       indexes = {
           @Index(name = "idx_violation_reporter", columnList = "reporter_id"),
           @Index(name = "idx_violation_content",  columnList = "content_type, content_id"),
           @Index(name = "idx_violation_status",   columnList = "status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who submitted this report. */
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    /**
     * Type of content being reported.
     * Values: "REVIEW" | "LIST" | "USER"
     */
    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType;

    /** Primary key of the reported content in its respective table. */
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    /** Free-text reason provided by the reporter (e.g. "Spam", "Hate speech"). */
    @Column(nullable = false, length = 500)
    private String reason;

    /**
     * Moderation resolution status.
     * Values: "pending" | "resolved" | "dismissed"
     */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "pending";
    }
}

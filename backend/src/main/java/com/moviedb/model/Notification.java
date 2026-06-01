package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In-app notification delivered to a user.
 *
 * WHY a simple polling model (not WebSocket)?
 *   The GET /api/notifications endpoint is polled by the frontend on page load.
 *   This keeps the backend stateless (fits JWT + REST) and is sufficient for
 *   the current scale. Upgrade to SSE/WebSocket when real-time is required.
 */
@Entity
@Table(name = "notifications",
       indexes = {
           @Index(name = "idx_notification_user", columnList = "user_id"),
           @Index(name = "idx_notification_read", columnList = "user_id, is_read")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** "FOLLOW" | "REVIEW" | "RATING" */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean read;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (read == null) read = false;
    }
}

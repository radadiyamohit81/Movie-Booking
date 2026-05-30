package com.moviedb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records that a user has watched a specific movie.
 *
 * This enables "Continue Watching" and personalised recommendations in future
 * iterations. For now it's a simple append-only log — same movie can appear
 * multiple times if watched on different dates.
 */
@Entity
@Table(name = "watch_history",
       indexes = {
           @Index(name = "idx_wh_user",  columnList = "user_id"),
           @Index(name = "idx_wh_movie", columnList = "movie_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime watchedAt;

    @PrePersist
    protected void onCreate() {
        watchedAt = LocalDateTime.now();
    }
}

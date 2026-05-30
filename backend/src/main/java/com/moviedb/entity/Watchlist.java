package com.moviedb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join between a User and a Movie they want to watch later.
 *
 * WHY unique constraint on (userId, movieId)?
 *   A user cannot add the same movie to their watchlist twice. The constraint
 *   is enforced at DB level (idempotent POST = 409 Conflict from service layer).
 */
@Entity
@Table(name = "watchlist",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_watchlist_user_movie",
           columnNames = {"user_id", "movie_id"}
       ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}

package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User-authored review for a Movie.
 *
 * WHY status as String ("published" / "flagged")?
 *   Reviews go through a moderation lifecycle. A String allows the admin to
 *   flag reviews without requiring a full enum update deployment. A dedicated
 *   ReviewStatus enum is the next evolution once moderation workflows are built.
 */
@Entity
@Table(name = "reviews",
       indexes = {
           @Index(name = "idx_review_movie", columnList = "movie_id"),
           @Index(name = "idx_review_user",  columnList = "user_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Boolean spoiler;

    /** "published" | "flagged" */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "published";
        if (spoiler == null) spoiler = false;
    }
}

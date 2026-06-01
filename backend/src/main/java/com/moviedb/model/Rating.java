package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores a user's numeric rating for a specific movie.
 *
 * WHY store movieId / userId as plain Longs instead of @ManyToOne?
 *   Avoids bidirectional relationship complexity and prevents Hibernate from
 *   issuing N+1 SELECT queries when listing ratings. The foreign key constraint
 *   discipline lives in the service layer (validate movie/user existence before
 *   saving). This pattern mirrors how high-scale services (e.g. Amazon) model
 *   cross-aggregate references.
 *
 * WHY a unique constraint on (userId, movieId)?
 *   One user may only rate a given movie once. The DB constraint is the last
 *   line of defence even if the service layer check is bypassed.
 */
@Entity
@Table(name = "ratings",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_rating_user_movie",
           columnNames = {"user_id", "movie_id"}
       ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Decimal rating, e.g. 7.5 */
    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

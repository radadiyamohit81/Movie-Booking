package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core domain entity representing a movie or TV series.
 *
 * WHY @ElementCollection(fetch = EAGER)?
 *   writers, stars, genre are stored in separate join-tables
 *   (movie_writers, movie_stars, movie_genre).  JPQL EXISTS subqueries
 *   inside JPA Specifications require these collections to be fully loaded.
 *   Hibernate silently returns empty results for LAZY collections in subqueries,
 *   causing hard-to-debug failures. EAGER avoids this at a cost of a few extra
 *   joins — acceptable for a read-heavy movie browsing workload.
 *
 * WHY @PrePersist / @PreUpdate?
 *   Centralises timestamp management. Service code never touches createdAt /
 *   updatedAt; the JPA event listener sets them automatically.
 *
 * WHY @Enumerated(STRING)?
 *   Stores 'movie'/'series' and 'published'/'upcoming'/'draft' as readable
 *   strings in the DB column instead of ordinal integers — migrations and
 *   ad-hoc SQL queries stay human-readable.
 */
@Entity
@Table(name = "movies",
       indexes = {
           @Index(name = "idx_movies_status",     columnList = "status"),
           @Index(name = "idx_movies_type",       columnList = "type"),
           @Index(name = "idx_movies_rating",     columnList = "rating"),
           @Index(name = "idx_movies_popularity", columnList = "popularity")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    private Integer releaseYear;

    /** Human-readable duration: "2h 32m" */
    @Column(length = 20)
    private String duration;

    /** Aggregate decimal rating, e.g. 8.5 */
    private Double rating;

    /** Popularity score 0-100 used for featured/trending ordering */
    private Integer popularity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String director;

    /**
     * WHY separate table via @ElementCollection?
     *   These are value-type lists (plain Strings), not entities. Using
     *   @ElementCollection avoids the overhead of a full @ManyToMany join
     *   entity while still allowing JPQL subquery searches.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_writers",
                     joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "writer")
    @Builder.Default
    private List<String> writers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_stars",
                     joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "star")
    @Builder.Default
    private List<String> stars = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_genre",
                     joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    @Builder.Default
    private List<String> genre = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Inner enums (lifecycle + content type) ────────────────────────────

    /**
     * Controls the content lifecycle / visibility of a Movie.
     *
     * published  — visible to end-users in all public search/browse endpoints.
     * upcoming   — visible on the "upcoming" home section but not searchable.
     * draft      — admin-only; hidden from all public APIs.
     *
     * WHY a lifecycle status instead of a boolean?
     *   A binary "isPublished" flag cannot express "coming soon" without a second
     *   boolean, creating impossible state combinations. An enum models the finite
     *   state machine correctly and is self-documenting.
     */
    public enum MovieStatus {
        PUBLISHED,
        UPCOMING,
        DRAFT
    }

    /**
     * Discriminates between a theatrical/streaming movie and a TV series.
     *
     * WHY an enum (not a plain String)?
     *   - Compile-time safety: typos in code caught at build time, not runtime.
     *   - @Enumerated(STRING) stores the human-readable name in the DB column.
     *   - Adding a new type (e.g. SHORT) is a one-line change here.
     */
    public enum MovieType {
        MOVIE,
        SERIES
    }
}

package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User-curated movie list (e.g. "Best Sci-Fi of the 90s").
 *
 * WHY store movieIds as @ElementCollection instead of @ManyToMany with Movie?
 *   - Avoids a bidirectional relationship that would require Movie to hold a
 *     reference to every CustomList it appears in — a very wide coupling.
 *   - CustomList is the aggregate root; Movie IDs are value references.
 *   - Deletion is simpler: deleting a CustomList cascades to custom_list_movie_ids
 *     without touching the Movie table.
 *
 * WHY Boolean isPublic (wrapper, not primitive)?
 *   Lombok generates getIsPublic() for Boolean (wrapper), which Jackson
 *   serialises as JSON key "isPublic" — matching the API contract.
 *   A primitive boolean would generate isIsPublic() with double 'is', breaking JSON.
 */
@Entity
@Table(name = "custom_lists",
       indexes = @Index(name = "idx_custom_list_user", columnList = "user_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CustomList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "custom_list_movie_ids",
                     joinColumns = @JoinColumn(name = "list_id"))
    @Column(name = "movie_id")
    @Builder.Default
    private List<Long> movieIds = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isPublic == null) isPublic = false;
    }
}

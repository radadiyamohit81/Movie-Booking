package com.moviedb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Directed social graph edge: follower → following.
 *
 * WHY a separate entity and not a self-referential @ManyToMany on User?
 *   A dedicated Follow table allows attaching metadata (createdAt, status)
 *   to the relationship itself — something @ManyToMany join tables cannot do
 *   without a wrapper entity anyway. Explicit IDs also make DELETE by
 *   (followerId, followingId) straightforward.
 */
@Entity
@Table(name = "follows",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_follow_pair",
           columnNames = {"follower_id", "following_id"}
       ),
       indexes = {
           @Index(name = "idx_follow_follower",  columnList = "follower_id"),
           @Index(name = "idx_follow_following", columnList = "following_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id",  nullable = false)
    private Long followerId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

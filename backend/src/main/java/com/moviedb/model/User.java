package com.moviedb.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Platform user — stores credentials, role, and account metadata.
 *
 * WHY @Table(name = "app_user")?
 *   "user" is a reserved keyword in SQL (especially H2 and PostgreSQL).
 *   Naming the table "app_user" avoids quoting or runtime errors when
 *   Hibernate generates DDL.
 *
 * WHY store role as a plain String instead of an enum or role table?
 *   Spring Security's GrantedAuthority interface expects "ROLE_ADMIN" /
 *   "ROLE_USER" strings. A plain column keeps the mapping trivial — one
 *   String → one GrantedAuthority, no join. For a project with 2 roles
 *   this is the pragmatic choice. Swap to a @ManyToMany roles table when
 *   the role matrix grows.
 *
 * Password field is BCrypt-hashed before it ever reaches this entity
 * (AuthService responsibility). @JsonIgnore would protect it at the
 * serialisation layer too, but we never return User entities from
 * controllers — we return AuthResponse DTOs instead.
 */
@Entity
@Table(name = "app_user",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
           @UniqueConstraint(name = "uk_user_email",    columnNames = "email")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** BCrypt-hashed password. Never exposed in any API response. */
    @Column(nullable = false)
    private String password;

    /**
     * Value is either "ROLE_USER" or "ROLE_ADMIN".
     * Spring Security loads this directly as a GrantedAuthority.
     */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

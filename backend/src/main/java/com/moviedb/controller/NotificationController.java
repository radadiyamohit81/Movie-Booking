package com.moviedb.controller;

import com.moviedb.entity.Notification;
import com.moviedb.repository.UserRepository;
import com.moviedb.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository      userRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                notificationService.getNotifications(resolveUserId(principal)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id, resolveUserId(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}

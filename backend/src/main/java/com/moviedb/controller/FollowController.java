package com.moviedb.controller;

import com.moviedb.entity.Follow;
import com.moviedb.repository.UserRepository;
import com.moviedb.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService  followService;
    private final UserRepository userRepository;

    @PostMapping("/{userId}")
    public ResponseEntity<Follow> follow(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long userId) {
        Long followerId = resolveUserId(principal);
        return ResponseEntity.ok(followService.followUser(followerId, userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long userId) {
        Long followerId = resolveUserId(principal);
        followService.unfollowUser(followerId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public ResponseEntity<List<Follow>> getFollowers(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(followService.getFollowers(resolveUserId(principal)));
    }

    @GetMapping("/following")
    public ResponseEntity<List<Follow>> getFollowing(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(followService.getFollowing(resolveUserId(principal)));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}

package com.moviedb.controller;

import com.moviedb.dto.AddMovieToListRequest;
import com.moviedb.dto.CustomListRequest;
import com.moviedb.entity.CustomList;
import com.moviedb.repository.UserRepository;
import com.moviedb.service.CustomListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class CustomListController {

    private final CustomListService customListService;
    private final UserRepository    userRepository;

    @GetMapping
    public ResponseEntity<List<CustomList>> getLists(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(customListService.getLists(resolveUserId(principal)));
    }

    @PostMapping
    public ResponseEntity<CustomList> createList(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CustomListRequest req) {
        return ResponseEntity.ok(customListService.createList(resolveUserId(principal), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomList> updateList(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestBody CustomListRequest req) {
        return ResponseEntity.ok(customListService.updateList(id, resolveUserId(principal), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        customListService.deleteList(id, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/movies")
    public ResponseEntity<CustomList> addMovie(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody AddMovieToListRequest req) {
        return ResponseEntity.ok(customListService.addMovieToList(id, resolveUserId(principal), req));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}

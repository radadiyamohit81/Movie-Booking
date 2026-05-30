package com.moviedb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Request body for POST /api/watchlist. */
@Data
public class WatchlistRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;
}

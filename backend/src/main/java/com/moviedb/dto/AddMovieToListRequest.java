package com.moviedb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Request body for POST /api/lists/{id}/movies. */
@Data
public class AddMovieToListRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;
}

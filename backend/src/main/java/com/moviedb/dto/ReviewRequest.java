package com.moviedb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /api/reviews.
 */
@Data
public class ReviewRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotBlank(message = "Review content is required")
    @Size(max = 2000, message = "Review must not exceed 2000 characters")
    private String content;

    private boolean spoiler = false;
}

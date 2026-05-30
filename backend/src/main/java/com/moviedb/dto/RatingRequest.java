package com.moviedb.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /api/ratings.
 *
 * WHY Double (not Integer)?
 *   Ratings support decimals (e.g. 7.5, 8.3) matching the Movie.rating field.
 *   Using Double here mirrors the DB column type and avoids silent truncation.
 */
@Data
public class RatingRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must not exceed 10.0")
    private Double rating;
}

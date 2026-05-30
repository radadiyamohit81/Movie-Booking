package com.moviedb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request/response for the full movie admin payload (POST/PUT /api/admin/movies). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieAdminRequest {

    private String title;
    private Integer releaseYear;
    private String duration;
    private Double rating;
    private Integer popularity;
    private String description;
    private String director;
    private java.util.List<String> writers;
    private java.util.List<String> stars;
    private java.util.List<String> genre;
    private String type;
    private String status;
}

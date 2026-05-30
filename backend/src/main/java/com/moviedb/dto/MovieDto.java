package com.moviedb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for Movie responses.
 *
 * WHY a separate DTO and not expose the entity directly?
 *   - Entity exposes JPA internals (@ElementCollection proxy objects, lazy wrappers).
 *   - DTO controls exactly what the API contract looks like; fields can be renamed,
 *     added, or removed without touching the persistence model.
 *   - _id as String matches the MongoDB-style convention the frontend expects,
 *     while the DB stores it as a Long — the conversion is the DTO's job.
 *
 * WHY @Builder?
 *   The service's toDto() method uses the builder pattern for readable,
 *   named-argument construction without a massive constructor call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {

    /**
     * WHY @JsonProperty("_id")?
     *   The API contract specifies the field name "_id" (MongoDB-style) so the
     *   React frontend can treat it uniformly. The Java field is named "id" to
     *   keep it idiomatic; Jackson handles the rename at serialisation time.
     */
    @JsonProperty("_id")
    private String id;

    private String title;
    private Integer year;
    private String duration;
    private Double rating;
    private Integer popularity;
    private String description;
    private String director;
    private List<String> writers;
    private List<String> stars;
    private List<String> genre;
    private String type;
    private String status;
    private String createdAt;
    private String updatedAt;
}

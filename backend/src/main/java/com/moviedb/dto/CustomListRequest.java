package com.moviedb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Request body for POST and PUT /api/lists. */
@Data
public class CustomListRequest {

    @NotBlank(message = "List name is required")
    @Size(max = 100, message = "List name must not exceed 100 characters")
    private String name;

    private Boolean isPublic = false;
}

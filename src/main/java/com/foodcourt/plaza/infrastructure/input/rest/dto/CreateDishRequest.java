package com.foodcourt.plaza.infrastructure.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /dishes}. Shape checks fail here, before the use case.
 * The restaurant, the owner role, and {@code active} are not decided here.
 */
public record CreateDishRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Positive Integer price,
        @NotBlank @Size(max = 500) @Pattern(regexp = ".*\\p{L}.*") String description,
        @NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://\\S+$") String imageUrl,
        @NotBlank @Size(max = 50) String category,
        @NotNull @Positive Long restaurantId) {
}

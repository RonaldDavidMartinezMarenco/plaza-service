package com.foodcourt.plaza.infrastructure.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /restaurants}. Shape checks fail here, before the use case.
 * The owner role and a duplicate NIT are not decided here.
 */
public record CreateRestaurantRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = ".*[^0-9].*") String name,
        @NotBlank @Size(max = 15) @Pattern(regexp = "^[0-9]+$") String nit,
        @NotBlank @Size(max = 200) String address,
        @NotBlank @Size(max = 13) @Pattern(regexp = "^\\+?[0-9]+$") String phone,
        @NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://\\S+$") String logoUrl,
        @NotNull @Positive Long ownerId) {
}

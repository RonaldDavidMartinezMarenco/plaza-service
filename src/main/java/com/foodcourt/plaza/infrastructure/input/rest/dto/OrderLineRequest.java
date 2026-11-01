package com.foodcourt.plaza.infrastructure.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * One line of {@code POST /orders}. Shape checks fail here, before the use case.
 * Jackson 3 ignores unknown properties by default, so {@code rejectUnknown} fails the read.
 * A line cannot carry a price or a name.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record OrderLineRequest(
        @NotNull @Positive Long dishId,
        @NotNull @Positive @Max(50) Integer quantity) {

    @JsonAnySetter
    void rejectUnknown(String name, Object ignored) {
        throw new IllegalArgumentException("Unknown field: " + name);
    }
}

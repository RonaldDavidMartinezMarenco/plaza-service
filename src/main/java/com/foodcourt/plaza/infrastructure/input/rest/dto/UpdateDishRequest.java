package com.foodcourt.plaza.infrastructure.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PUT /dishes/{id}}. Only price and description may be sent.
 * The description must include a letter. An unknown field fails here, before
 * the use case. Jackson 3 ignores unknown properties by default, so
 * {@code rejectUnknown} is what actually fails the read.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record UpdateDishRequest(
        @NotNull @Positive Integer price,
        @NotBlank @Size(max = 500) @Pattern(regexp = ".*\\p{L}.*") String description) {

    @JsonAnySetter
    void rejectUnknown(String name, Object ignored) {
        throw new IllegalArgumentException("Unknown field: " + name);
    }
}

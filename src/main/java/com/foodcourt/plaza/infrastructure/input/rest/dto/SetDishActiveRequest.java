package com.foodcourt.plaza.infrastructure.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code PUT /dishes/{id}/active}. Only the requested state is sent.
 * {@code Boolean} keeps a missing value null so it fails here; a primitive
 * would turn a missing field into false and disable the dish. An unknown field
 * fails here, before the use case. Jackson 3 ignores unknown properties by
 * default, so {@code rejectUnknown} is what actually fails the read.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record SetDishActiveRequest(@NotNull Boolean active) {

    @JsonAnySetter
    void rejectUnknown(String name, Object ignored) {
        throw new IllegalArgumentException("Unknown field: " + name);
    }
}

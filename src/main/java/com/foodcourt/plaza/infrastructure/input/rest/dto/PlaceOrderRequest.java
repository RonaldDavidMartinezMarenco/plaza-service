package com.foodcourt.plaza.infrastructure.input.rest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /orders}. Shape checks fail here, before the use case.
 * The customer, the status, and the menu are not decided here.
 * Jackson 3 ignores unknown properties by default, so {@code rejectUnknown} fails the read.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record PlaceOrderRequest(
        @NotNull @Positive Long restaurantId,
        @NotNull @Size(min = 1, max = 20) List<@NotNull @Valid OrderLineRequest> lines) {

    @JsonAnySetter
    void rejectUnknown(String name, Object ignored) {
        throw new IllegalArgumentException("Unknown field: " + name);
    }
}

package com.foodcourt.plaza.infrastructure.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Body of {@code POST /orders/{orderId}/delivery}. Shape checks fail here, before the use case.
 * The PIN is a string so a leading zero is kept. Jackson 3 ignores unknown properties by default,
 * so {@code rejectUnknown} fails the read.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record DeliverOrderRequest(
        @NotBlank @Pattern(regexp = "^\\d{6}$") String pin) {

    @JsonAnySetter
    void rejectUnknown(String name, Object ignored) {
        throw new IllegalArgumentException("Unknown field: " + name);
    }
}

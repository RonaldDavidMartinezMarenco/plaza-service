package com.foodcourt.plaza.infrastructure.input.rest.dto;

/** One stored line. The price stays on the dish. */
public record OrderLineResponse(long dishId, int quantity) {
}

package com.foodcourt.plaza.infrastructure.input.rest.dto;

/** One restaurant in the customer list. NIT, phone, address, and owner are omitted. */
public record RestaurantSummaryResponse(Long id, String name, String logoUrl) {
}

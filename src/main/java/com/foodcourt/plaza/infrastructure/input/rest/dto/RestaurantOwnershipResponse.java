package com.foodcourt.plaza.infrastructure.input.rest.dto;

/** Restaurant id and its owner account id. NIT, phone, and address are omitted. */
public record RestaurantOwnershipResponse(Long id, Long ownerId) {
}

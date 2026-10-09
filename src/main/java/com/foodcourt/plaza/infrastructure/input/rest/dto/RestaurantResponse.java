package com.foodcourt.plaza.infrastructure.input.rest.dto;

/** Restaurant returned after creation. */
public record RestaurantResponse(
        Long id,
        String name,
        String nit,
        String address,
        String phone,
        String logoUrl,
        Long ownerId) {
}

package com.foodcourt.plaza.infrastructure.input.rest.dto;

/** Dish returned after creation. {@code active} is true for a newly created dish. */
public record DishResponse(
        Long id,
        String name,
        Integer price,
        String description,
        String imageUrl,
        String category,
        Long restaurantId,
        boolean active) {
}

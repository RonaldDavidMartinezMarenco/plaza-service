package com.foodcourt.plaza.infrastructure.input.rest.dto;

/** One active dish of a restaurant menu. */
public record DishSummaryResponse(
        Long id,
        Long restaurantId,
        String name,
        Integer price,
        String description,
        String imageUrl,
        String category) {
}

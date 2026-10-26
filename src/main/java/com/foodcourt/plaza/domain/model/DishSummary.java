package com.foodcourt.plaza.domain.model;

/**
 * Dish fields a customer needs to choose one. Inactive dishes are not part of a menu,
 * so this type has no {@code active} flag.
 */
public record DishSummary(
        Long id,
        Long restaurantId,
        String name,
        Integer price,
        String description,
        String imageUrl,
        String category) {
}

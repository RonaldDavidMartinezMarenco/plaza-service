package com.foodcourt.plaza.domain.model;

/**
 * Input of dish creation. {@code id} is assigned by PostgreSQL on insert.
 * Name, description, and category may still have surrounding spaces; the use case trims them.
 * {@code active} is not chosen here: a new dish is always active.
 */
public record NewDish(
        String name,
        Integer price,
        String description,
        String imageUrl,
        String category,
        Long restaurantId) {
}

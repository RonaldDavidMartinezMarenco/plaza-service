package com.foodcourt.plaza.domain.model;

/**
 * Persisted dish. {@code id} is null until PostgreSQL assigns it.
 * {@code restaurantId} points at a row in {@code restaurant}.
 */
public record Dish(
        Long id,
        Long restaurantId,
        String name,
        Integer price,
        String description,
        String imageUrl,
        String category,
        boolean active) {

    /** Copy with only {@code active} replaced. The caller decides the value. */
    public Dish withActive(boolean active) {
        return new Dish(id, restaurantId, name, price, description, imageUrl, category, active);
    }
}

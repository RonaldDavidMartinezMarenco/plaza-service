package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;

/**
 * Restaurant name stored without surrounding spaces and with single spaces inside.
 * At least two characters, at most 100, one letter, and only letters, digits,
 * spaces, and {@code - ' . , &}. {@code Café} and {@code Cafe} stay distinct.
 */
public record RestaurantName(String value) {

    static final int MAX = 100;

    public RestaurantName {
        String normalized = TextShape.collapse(value);
        if (!TextShape.matches(normalized, MAX, TextShape.NAME)) {
            throw new InvalidRestaurantName();
        }
        value = normalized;
    }
}

package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishText;

/**
 * Dish description stored without surrounding spaces and with single spaces inside.
 * Empty text is rejected. A value needs one letter, between 2 and 500 characters,
 * and may include sentence punctuation. A newline is rejected.
 */
public record DishDescription(String value) {

    static final int MAX = 500;

    public DishDescription {
        String normalized = TextShape.collapse(value);
        if (normalized.isEmpty()) {
            throw new InvalidDishText();
        }
        requireShape(normalized);
        value = normalized;
    }

    private static void requireShape(String normalized) {
        if (!TextShape.matches(normalized, MAX, TextShape.DESCRIPTION)) {
            throw new InvalidDishDescription();
        }
    }
}

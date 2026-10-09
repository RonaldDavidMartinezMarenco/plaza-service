package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidDishText;

/**
 * Dish name, description, or category stored without surrounding spaces.
 * A value that is empty after trimming is rejected.
 */
public record DishText(String value) {

    public DishText {
        String trimmed = trimmed(value);
        if (trimmed.isEmpty()) {
            throw new InvalidDishText();
        }
        value = trimmed;
    }

    private static String trimmed(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}

package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidDishText;

/**
 * Dish name or category stored without surrounding spaces and with single spaces inside.
 * A name allows 100 characters. A category allows 50. Both need one letter and the name signs.
 */
public record DishText(String value) {

    static final int NAME_MAX = 100;
    static final int CATEGORY_MAX = 50;

    public DishText {
        value = checked(value, NAME_MAX);
    }

    public static DishText category(String raw) {
        return new DishText(checked(raw, CATEGORY_MAX));
    }

    private static String checked(String raw, int max) {
        String normalized = TextShape.collapse(raw);
        if (!TextShape.matches(normalized, max, TextShape.NAME)) {
            throw new InvalidDishText();
        }
        return normalized;
    }
}

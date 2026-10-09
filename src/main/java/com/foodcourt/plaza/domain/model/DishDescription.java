package com.foodcourt.plaza.domain.model;

import java.util.regex.Pattern;

import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishText;

/**
 * Dish description stored without surrounding spaces.
 * Empty text is rejected. A value made only of digits or symbols is rejected;
 * it must include at least one letter.
 */
public record DishDescription(String value) {

    private static final Pattern WORD = Pattern.compile("\\p{L}");

    public DishDescription {
        String trimmed = trimmed(value);
        if (trimmed.isEmpty()) {
            throw new InvalidDishText();
        }
        requireWord(trimmed);
        value = trimmed;
    }

    private static String trimmed(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static void requireWord(String trimmed) {
        if (!WORD.matcher(trimmed).find()) {
            throw new InvalidDishDescription();
        }
    }
}

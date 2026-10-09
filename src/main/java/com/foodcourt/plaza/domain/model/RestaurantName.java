package com.foodcourt.plaza.domain.model;

import java.util.regex.Pattern;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;

/**
 * Restaurant name stored without surrounding spaces.
 * Digits are allowed. A name that is empty or only digits after trimming is rejected.
 * The column check {@code name !~ '^[0-9]+$'} does not see those spaces.
 */
public record RestaurantName(String value) {

    private static final Pattern DIGITS_ONLY = Pattern.compile("^[0-9]+$");

    public RestaurantName {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || DIGITS_ONLY.matcher(trimmed).matches()) {
            throw new InvalidRestaurantName();
        }
        value = trimmed;
    }
}

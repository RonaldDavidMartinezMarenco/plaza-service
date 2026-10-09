package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantAddress;

/**
 * Address stored without surrounding spaces and with single spaces inside.
 * Same letters and signs as a name, plus {@code #} and {@code /}.
 */
public record RestaurantAddress(String value) {

    static final int MAX = 200;

    public RestaurantAddress {
        String normalized = TextShape.collapse(value);
        if (!TextShape.matches(normalized, MAX, TextShape.ADDRESS)) {
            throw new InvalidRestaurantAddress();
        }
        value = normalized;
    }
}

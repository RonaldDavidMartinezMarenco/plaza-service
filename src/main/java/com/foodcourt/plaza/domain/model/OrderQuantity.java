package com.foodcourt.plaza.domain.model;

import com.foodcourt.plaza.domain.exception.InvalidOrderQuantity;

/** Dishes of one kind in a line. A whole number from 1 to {@link #MAX}. */
public record OrderQuantity(Integer value) {

    public static final int MAX = 50;

    public OrderQuantity {
        if (value == null || value < 1 || value > MAX) {
            throw new InvalidOrderQuantity();
        }
    }
}

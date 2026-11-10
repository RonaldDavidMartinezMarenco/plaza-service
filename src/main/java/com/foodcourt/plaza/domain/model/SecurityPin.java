package com.foodcourt.plaza.domain.model;

/**
 * Six-digit security PIN. Leading zeros are part of the value.
 * A value that is not six digits is rejected before it can be hashed or sent.
 */
public record SecurityPin(String value) {

    public static final int LENGTH = 6;

    public SecurityPin {
        if (value == null || !value.matches("\\d{" + LENGTH + "}")) {
            throw new IllegalArgumentException("A security PIN is 6 digits");
        }
    }
}

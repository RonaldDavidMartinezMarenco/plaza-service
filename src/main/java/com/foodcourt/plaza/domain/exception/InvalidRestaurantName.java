package com.foodcourt.plaza.domain.exception;

/** The name fails the stored shape. Mapped to HTTP 400. */
public final class InvalidRestaurantName extends RuntimeException {

    public InvalidRestaurantName() {
        super("The restaurant name must contain a letter and only the allowed characters");
    }
}

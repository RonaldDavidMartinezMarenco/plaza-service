package com.foodcourt.plaza.domain.exception;

/** The name is blank or only digits after trimming. Mapped to HTTP 400. */
public final class InvalidRestaurantName extends RuntimeException {

    public InvalidRestaurantName() {
        super("The restaurant name cannot be blank or digits only");
    }
}

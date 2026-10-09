package com.foodcourt.plaza.domain.exception;

/** The address fails the stored shape. Mapped to HTTP 400. */
public final class InvalidRestaurantAddress extends RuntimeException {

    public InvalidRestaurantAddress() {
        super("The address must contain a letter and only the allowed characters");
    }
}

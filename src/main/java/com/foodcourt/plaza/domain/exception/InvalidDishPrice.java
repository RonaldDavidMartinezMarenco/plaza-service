package com.foodcourt.plaza.domain.exception;

/** The price is missing or not greater than zero. Mapped to HTTP 400. */
public final class InvalidDishPrice extends RuntimeException {

    public InvalidDishPrice() {
        super("The dish price must be a positive integer");
    }
}

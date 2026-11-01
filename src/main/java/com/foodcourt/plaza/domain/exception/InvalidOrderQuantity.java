package com.foodcourt.plaza.domain.exception;

/** The quantity is missing, not positive, or above the line limit. Mapped to HTTP 400. */
public final class InvalidOrderQuantity extends RuntimeException {

    public InvalidOrderQuantity() {
        super("The quantity must be a whole number from 1 to 50");
    }
}

package com.foodcourt.plaza.domain.exception;

/** A description fails the stored shape. Mapped to HTTP 400. */
public final class InvalidDishDescription extends RuntimeException {

    public InvalidDishDescription() {
        super("The description must contain a letter and only the allowed characters");
    }
}

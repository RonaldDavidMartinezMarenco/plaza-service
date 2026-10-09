package com.foodcourt.plaza.domain.exception;

/** A dish name, description, or category fails the stored shape. Mapped to HTTP 400. */
public final class InvalidDishText extends RuntimeException {

    public InvalidDishText() {
        super("The dish text must contain a letter and only the allowed characters");
    }
}

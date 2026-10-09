package com.foodcourt.plaza.domain.exception;

/** A dish name, description, or category is blank after trimming. Mapped to HTTP 400. */
public final class InvalidDishText extends RuntimeException {

    public InvalidDishText() {
        super("The dish text cannot be blank");
    }
}

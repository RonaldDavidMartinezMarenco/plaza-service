package com.foodcourt.plaza.domain.exception;

/** A description has no letter after trimming. Mapped to HTTP 400. */
public final class InvalidDishDescription extends RuntimeException {

    public InvalidDishDescription() {
        super("The description must include a word");
    }
}

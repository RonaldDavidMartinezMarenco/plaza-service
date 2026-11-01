package com.foodcourt.plaza.domain.exception;

/** The dish exists and belongs to the restaurant, but it is not offered. Mapped to HTTP 409. */
public final class DishInactive extends RuntimeException {

    public DishInactive() {
        super("This dish is not available");
    }
}

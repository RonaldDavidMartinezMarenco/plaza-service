package com.foodcourt.plaza.domain.exception;

/** The dish belongs to another restaurant. Mapped to HTTP 409. */
public final class DishRestaurantMismatch extends RuntimeException {

    public DishRestaurantMismatch() {
        super("The dish does not belong to this restaurant");
    }
}

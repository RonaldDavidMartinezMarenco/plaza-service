package com.foodcourt.plaza.domain.exception;

/** No restaurant row exists for this id. Mapped to HTTP 404. */
public final class RestaurantNotFound extends RuntimeException {

    public RestaurantNotFound() {
        super("No restaurant exists for this id");
    }
}

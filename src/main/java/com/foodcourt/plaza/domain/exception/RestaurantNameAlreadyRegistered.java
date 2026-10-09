package com.foodcourt.plaza.domain.exception;

/** {@code lower(restaurant.name)} is already taken. Mapped to HTTP 409. */
public final class RestaurantNameAlreadyRegistered extends RuntimeException {

    public RestaurantNameAlreadyRegistered() {
        super("Restaurant name is already registered");
    }
}

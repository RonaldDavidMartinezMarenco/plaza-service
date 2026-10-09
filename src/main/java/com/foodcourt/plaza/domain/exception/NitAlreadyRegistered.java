package com.foodcourt.plaza.domain.exception;

/** {@code restaurant.nit} is already taken. Mapped to HTTP 409. */
public final class NitAlreadyRegistered extends RuntimeException {

    public NitAlreadyRegistered() {
        super("NIT is already registered");
    }
}

package com.foodcourt.plaza.domain.exception;

/** The PIN has a valid shape and does not match the stored hash. Mapped to HTTP 409. */
public final class PinDoesNotMatch extends RuntimeException {

    public PinDoesNotMatch() {
        super("The security PIN does not match");
    }
}

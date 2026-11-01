package com.foodcourt.plaza.domain.exception;

/**
 * The customer already has an order in {@code PENDING}, {@code IN_PREPARATION}, or {@code READY}.
 * Mapped to HTTP 409.
 */
public final class ActiveOrderExists extends RuntimeException {

    public ActiveOrderExists() {
        super("The customer already has an order in progress");
    }
}

package com.foodcourt.plaza.domain.exception;

/** The order has no lines. Mapped to HTTP 400. */
public final class EmptyOrder extends RuntimeException {

    public EmptyOrder() {
        super("An order needs at least one dish");
    }
}

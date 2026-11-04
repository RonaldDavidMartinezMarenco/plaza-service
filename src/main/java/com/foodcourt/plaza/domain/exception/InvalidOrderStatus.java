package com.foodcourt.plaza.domain.exception;

/** The status filter is missing, repeated, or not one of the five stored names. Mapped to HTTP 400. */
public final class InvalidOrderStatus extends RuntimeException {

    public InvalidOrderStatus() {
        super("The status must be one of PENDING, IN_PREPARATION, READY, DELIVERED, CANCELLED");
    }
}

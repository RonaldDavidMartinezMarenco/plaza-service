package com.foodcourt.plaza.domain.exception;

/** The same dish was sent twice. Mapped to HTTP 400. */
public final class DuplicateOrderLine extends RuntimeException {

    public DuplicateOrderLine() {
        super("A dish can appear only once in the order");
    }
}

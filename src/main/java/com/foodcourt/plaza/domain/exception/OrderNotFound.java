package com.foodcourt.plaza.domain.exception;

/** No order of this restaurant exists for the id. Mapped to HTTP 404. */
public final class OrderNotFound extends RuntimeException {

    public OrderNotFound() {
        super("No order exists for this id");
    }
}

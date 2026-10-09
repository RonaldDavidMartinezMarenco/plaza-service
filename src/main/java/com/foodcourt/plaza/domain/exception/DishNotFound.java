package com.foodcourt.plaza.domain.exception;

/** No dish row exists for this id. Mapped to HTTP 404. */
public final class DishNotFound extends RuntimeException {

    public DishNotFound() {
        super("No dish exists for this id");
    }
}

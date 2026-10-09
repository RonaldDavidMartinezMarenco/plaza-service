package com.foodcourt.plaza.domain.exception;

/** user-service has no account for this id. Mapped to HTTP 404. */
public final class OwnerNotFound extends RuntimeException {

    public OwnerNotFound() {
        super("No account exists for this id");
    }
}

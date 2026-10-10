package com.foodcourt.plaza.domain.exception;

/** The caller is authenticated, but the role or the restaurant does not match. Mapped to HTTP 403. */
public final class AccessDenied extends RuntimeException {

    public AccessDenied() {
        super("You do not have permission for this action");
    }
}

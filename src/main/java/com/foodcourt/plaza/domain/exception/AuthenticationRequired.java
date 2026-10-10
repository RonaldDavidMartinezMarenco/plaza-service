package com.foodcourt.plaza.domain.exception;

/** No authenticated caller is available. Mapped to HTTP 401. */
public final class AuthenticationRequired extends RuntimeException {

    public AuthenticationRequired() {
        super("Authentication is required");
    }
}

package com.foodcourt.plaza.domain.exception;

/** user-service did not answer the account lookup. Mapped to HTTP 503. */
public final class OwnerDirectoryUnavailable extends RuntimeException {

    public OwnerDirectoryUnavailable() {
        super("The user service is unavailable");
    }
}

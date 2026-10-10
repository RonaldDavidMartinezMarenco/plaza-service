package com.foodcourt.plaza.domain.exception;

/** user-service refused to read the account. Mapped to HTTP 403. */
public final class AccountLookupRejected extends RuntimeException {

    public AccountLookupRejected() {
        super("The account could not be read");
    }
}

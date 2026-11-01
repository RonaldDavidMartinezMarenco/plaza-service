package com.foodcourt.plaza.domain.exception;

/** The live account is not a customer. Mapped to HTTP 403. */
public final class CustomerRoleRequired extends RuntimeException {

    public CustomerRoleRequired() {
        super("The account is not a customer");
    }
}

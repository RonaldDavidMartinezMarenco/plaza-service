package com.foodcourt.plaza.domain.exception;

/** The account exists, but its role is not owner. Mapped to HTTP 403. */
public final class OwnerRoleRequired extends RuntimeException {

    public OwnerRoleRequired() {
        super("The account must have the owner role");
    }
}

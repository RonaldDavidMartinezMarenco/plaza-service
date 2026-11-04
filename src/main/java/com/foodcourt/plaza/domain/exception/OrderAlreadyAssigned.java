package com.foodcourt.plaza.domain.exception;

/**
 * The pending order already names an employee, or the claim updated no row while it was still free.
 * Mapped to HTTP 409.
 */
public final class OrderAlreadyAssigned extends RuntimeException {

    public OrderAlreadyAssigned() {
        super("The order already has an assigned employee");
    }
}

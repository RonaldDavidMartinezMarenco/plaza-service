package com.foodcourt.plaza.domain.exception;

/** The caller is not the employee assigned to the order. Mapped to HTTP 409. */
public final class OrderAssignedToAnotherEmployee extends RuntimeException {

    public OrderAssignedToAnotherEmployee() {
        super("The order is assigned to another employee");
    }
}

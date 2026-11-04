package com.foodcourt.plaza.domain.exception;

/** An employee account has no restaurant. Mapped to HTTP 403. */
public final class EmployeeWithoutRestaurant extends RuntimeException {

    public EmployeeWithoutRestaurant() {
        super("The employee is not assigned to a restaurant");
    }
}

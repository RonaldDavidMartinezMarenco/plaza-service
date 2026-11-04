package com.foodcourt.plaza.domain.exception;

/** user-service has no account for the employee. Mapped to HTTP 404. */
public final class EmployeeNotFound extends RuntimeException {

    public EmployeeNotFound() {
        super("No employee account exists for this id");
    }
}

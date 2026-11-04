package com.foodcourt.plaza.domain.exception;

/** The live account is not an employee. Mapped to HTTP 403. */
public final class EmployeeRoleRequired extends RuntimeException {

    public EmployeeRoleRequired() {
        super("The account is not an employee");
    }
}

package com.foodcourt.plaza.application.usecase;

import com.foodcourt.plaza.domain.exception.EmployeeNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeRoleRequired;
import com.foodcourt.plaza.domain.exception.EmployeeWithoutRestaurant;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

/**
 * The caller's account still exists, its role is {@link Role#EMPLOYEE}, and it belongs to a restaurant.
 * The token is checked first. This confirms the row, because the restaurant id lives in user-service.
 */
final class EmployeeAccountRule {

    private final OwnerDirectory accounts;

    EmployeeAccountRule(OwnerDirectory accounts) {
        this.accounts = accounts;
    }

    long requireRestaurant(long employeeId) {
        Account account = accounts.findById(employeeId).orElseThrow(EmployeeNotFound::new);
        requireEmployeeRole(account);
        return requireRestaurantId(account);
    }

    private static void requireEmployeeRole(Account account) {
        if (account.role() != Role.EMPLOYEE) {
            throw new EmployeeRoleRequired();
        }
    }

    private static long requireRestaurantId(Account account) {
        if (account.restaurantId() == null) {
            throw new EmployeeWithoutRestaurant();
        }
        return account.restaurantId();
    }
}

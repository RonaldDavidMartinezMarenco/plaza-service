package com.foodcourt.plaza.application.usecase;

import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.CustomerRoleRequired;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

/**
 * The caller's account still exists in user-service and its role is {@link Role#CUSTOMER}.
 * The token is checked first. This confirms the row, because the id crosses databases.
 */
final class CustomerAccountRule {

    private final OwnerDirectory accounts;

    CustomerAccountRule(OwnerDirectory accounts) {
        this.accounts = accounts;
    }

    void requireCustomer(long customerId) {
        Account account = accounts.findById(customerId).orElseThrow(CustomerNotFound::new);
        requireCustomerRole(account);
    }

    private static void requireCustomerRole(Account account) {
        if (account.role() != Role.CUSTOMER) {
            throw new CustomerRoleRequired();
        }
    }
}

package com.foodcourt.plaza.application.usecase;

import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

/**
 * The account exists in user-service and its role is {@link Role#OWNER}.
 * Shared by restaurant creation and by {@link RestaurantOwnershipRule}.
 */
final class OwnerAccountRule {

    private final OwnerDirectory owners;

    OwnerAccountRule(OwnerDirectory owners) {
        this.owners = owners;
    }

    void requireOwner(long ownerId) {
        Account account = owners.findById(ownerId).orElseThrow(OwnerNotFound::new);
        if (account.role() != Role.OWNER) {
            throw new OwnerRoleRequired();
        }
    }
}

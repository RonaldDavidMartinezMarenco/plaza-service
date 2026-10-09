package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.Account;

/**
 * Reads an account from user-service.
 * An empty result means no row exists for that id.
 */
public interface OwnerDirectory {

    Optional<Account> findById(long id);
}

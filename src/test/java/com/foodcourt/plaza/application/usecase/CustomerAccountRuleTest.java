package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.CustomerRoleRequired;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

@ExtendWith(MockitoExtension.class)
class CustomerAccountRuleTest {

    private static final long CUSTOMER_ID = 9L;

    @Mock
    private OwnerDirectory accounts;

    private CustomerAccountRule rule;

    @BeforeEach
    void setUp() {
        rule = new CustomerAccountRule(accounts);
    }

    @Test
    void requireCustomerAcceptsACustomerAccount() {
        when(accounts.findById(CUSTOMER_ID)).thenReturn(Optional.of(new Account(CUSTOMER_ID, Role.CUSTOMER)));

        assertThatCode(() -> rule.requireCustomer(CUSTOMER_ID)).doesNotThrowAnyException();
    }

    @Test
    void requireCustomerRejectsAMissingAccount() {
        when(accounts.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rule.requireCustomer(CUSTOMER_ID)).isInstanceOf(CustomerNotFound.class);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "EMPLOYEE"})
    void requireCustomerRejectsAnAccountThatIsNotACustomer(Role role) {
        when(accounts.findById(CUSTOMER_ID)).thenReturn(Optional.of(new Account(CUSTOMER_ID, role)));

        assertThatThrownBy(() -> rule.requireCustomer(CUSTOMER_ID)).isInstanceOf(CustomerRoleRequired.class);
    }

    @Test
    void requireCustomerPropagatesADirectoryFailure() {
        when(accounts.findById(CUSTOMER_ID)).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> rule.requireCustomer(CUSTOMER_ID)).isInstanceOf(OwnerDirectoryUnavailable.class);
    }
}

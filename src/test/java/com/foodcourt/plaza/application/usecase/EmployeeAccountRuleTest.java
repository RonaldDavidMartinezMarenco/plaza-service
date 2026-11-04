package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.foodcourt.plaza.domain.exception.EmployeeNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeRoleRequired;
import com.foodcourt.plaza.domain.exception.EmployeeWithoutRestaurant;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

@ExtendWith(MockitoExtension.class)
class EmployeeAccountRuleTest {

    private static final long EMPLOYEE_ID = 5L;

    @Mock
    private OwnerDirectory accounts;

    private EmployeeAccountRule rule;

    @BeforeEach
    void setUp() {
        rule = new EmployeeAccountRule(accounts);
    }

    @Test
    void requireRestaurantReturnsTheEmployeeRestaurant() {
        when(accounts.findById(EMPLOYEE_ID)).thenReturn(Optional.of(new Account(EMPLOYEE_ID, Role.EMPLOYEE, 1L)));

        assertThat(rule.requireRestaurant(EMPLOYEE_ID)).isEqualTo(1L);
    }

    @Test
    void requireRestaurantRejectsAMissingAccount() {
        when(accounts.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rule.requireRestaurant(EMPLOYEE_ID)).isInstanceOf(EmployeeNotFound.class);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "CUSTOMER"})
    void requireRestaurantRejectsAnAccountThatIsNotAnEmployee(Role role) {
        when(accounts.findById(EMPLOYEE_ID)).thenReturn(Optional.of(new Account(EMPLOYEE_ID, role, 1L)));

        assertThatThrownBy(() -> rule.requireRestaurant(EMPLOYEE_ID)).isInstanceOf(EmployeeRoleRequired.class);
    }

    @Test
    void requireRestaurantRejectsAnEmployeeWithoutARestaurant() {
        when(accounts.findById(EMPLOYEE_ID)).thenReturn(Optional.of(new Account(EMPLOYEE_ID, Role.EMPLOYEE, null)));

        assertThatThrownBy(() -> rule.requireRestaurant(EMPLOYEE_ID)).isInstanceOf(EmployeeWithoutRestaurant.class);
    }

    @Test
    void requireRestaurantPropagatesADirectoryFailure() {
        when(accounts.findById(EMPLOYEE_ID)).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> rule.requireRestaurant(EMPLOYEE_ID)).isInstanceOf(OwnerDirectoryUnavailable.class);
    }
}

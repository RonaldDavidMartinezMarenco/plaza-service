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

import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

@ExtendWith(MockitoExtension.class)
class OwnerAccountRuleTest {

    private static final long OWNER_ID = 15L;

    @Mock
    private OwnerDirectory owners;

    private OwnerAccountRule rule;

    @BeforeEach
    void setUp() {
        rule = new OwnerAccountRule(owners);
    }

    @Test
    void requireOwnerAcceptsAnOwnerAccount() {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, Role.OWNER)));

        assertThatCode(() -> rule.requireOwner(OWNER_ID)).doesNotThrowAnyException();
    }

    @Test
    void requireOwnerRejectsAMissingAccount() {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rule.requireOwner(OWNER_ID)).isInstanceOf(OwnerNotFound.class);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void requireOwnerRejectsAnAccountThatIsNotAnOwner(Role role) {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, role)));

        assertThatThrownBy(() -> rule.requireOwner(OWNER_ID)).isInstanceOf(OwnerRoleRequired.class);
    }
}

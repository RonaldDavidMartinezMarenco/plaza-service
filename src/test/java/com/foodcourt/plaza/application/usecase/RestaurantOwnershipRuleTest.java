package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class RestaurantOwnershipRuleTest {

    private static final long RESTAURANT_ID = 4L;
    private static final long OWNER_ID = 15L;
    private static final Caller OWNER = new Caller(OWNER_ID, Role.OWNER);

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private OwnerDirectory owners;

    private RestaurantOwnershipRule rule;

    @BeforeEach
    void setUp() {
        rule = new RestaurantOwnershipRule(restaurants, new OwnerAccountRule(owners));
    }

    @Test
    void requireOwnerRoleAcceptsAnOwner() {
        assertThatCode(() -> rule.requireOwnerRole(OWNER)).doesNotThrowAnyException();
        verifyNoInteractions(restaurants, owners);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void requireOwnerRoleRejectsAnotherRole(Role role) {
        assertThatThrownBy(() -> rule.requireOwnerRole(new Caller(OWNER_ID, role))).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(restaurants, owners);
    }

    @Test
    void requireOwnedByReturnsTheRestaurantOfThatOwner() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, Role.OWNER)));

        assertThat(rule.requireOwnedBy(OWNER, RESTAURANT_ID)).isEqualTo(restaurant());
        verify(owners).findById(OWNER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void requireOwnedByRejectsAnotherRoleBeforeReading(Role role) {
        assertThatThrownBy(() -> rule.requireOwnedBy(new Caller(OWNER_ID, role), RESTAURANT_ID))
                .isInstanceOf(AccessDenied.class);
        verifyNoInteractions(restaurants, owners);
    }

    @Test
    void requireOwnedByRejectsAnUnknownRestaurant() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rule.requireOwnedBy(OWNER, RESTAURANT_ID)).isInstanceOf(RestaurantNotFound.class);
        verify(owners, never()).findById(anyLong());
    }

    @Test
    void requireOwnedByRejectsAnOwnerOfAnotherRestaurant() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));

        assertThatThrownBy(() -> rule.requireOwnedBy(new Caller(99L, Role.OWNER), RESTAURANT_ID))
                .isInstanceOf(AccessDenied.class);
        verify(owners, never()).findById(anyLong());
    }

    @Test
    void requireOwnedByRejectsAnUnknownOwner() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rule.requireOwnedBy(OWNER, RESTAURANT_ID)).isInstanceOf(OwnerNotFound.class);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void requireOwnedByRejectsAnAccountThatIsNotAnOwner(Role role) {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, role)));

        assertThatThrownBy(() -> rule.requireOwnedBy(OWNER, RESTAURANT_ID)).isInstanceOf(OwnerRoleRequired.class);
    }

    @Test
    void requireOwnedByPropagatesAnUnavailableUserService() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> rule.requireOwnedBy(OWNER, RESTAURANT_ID))
                .isInstanceOf(OwnerDirectoryUnavailable.class);
    }

    private static Restaurant restaurant() {
        return new Restaurant(
                RESTAURANT_ID,
                "Sushi 24",
                "900123456",
                "Calle 10",
                "+573005698325",
                "https://cdn.example.com/logos/sushi.png",
                OWNER_ID);
    }
}

package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class SetDishActiveUseCaseTest {

    private static final long DISH_ID = 21L;
    private static final long RESTAURANT_ID = 4L;
    private static final long OWNER_ID = 15L;
    private static final Caller OWNER = new Caller(OWNER_ID, Role.OWNER);
    private static final String IMAGE = "https://cdn.example.com/dishes/pasta.png";

    @Mock
    private DishRepository dishes;

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private OwnerDirectory owners;

    private SetDishActiveUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SetDishActiveUseCase(dishes, restaurants, owners);
    }

    @Test
    void setActiveDisablesTheDishAndLeavesTheOtherFields() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.updateActive(DISH_ID, false)).thenReturn(true);

        Dish updated = useCase.setActive(OWNER, DISH_ID, false);

        assertThat(updated).isEqualTo(stored(true).withActive(false));
        verify(dishes).updateActive(DISH_ID, false);
        verify(dishes, never()).save(any());
    }

    @Test
    void setActiveEnablesAnInactiveDish() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(false)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.updateActive(DISH_ID, true)).thenReturn(true);

        Dish updated = useCase.setActive(OWNER, DISH_ID, true);

        assertThat(updated.active()).isTrue();
        assertThat(updated.name()).isEqualTo("Pasta");
        assertThat(updated.price()).isEqualTo(12000);
        verify(dishes).updateActive(DISH_ID, true);
        verify(dishes, never()).save(any());
    }

    @Test
    void setActiveWritesTheStateTheDishAlreadyHas() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.updateActive(DISH_ID, true)).thenReturn(true);

        Dish updated = useCase.setActive(OWNER, DISH_ID, true);

        assertThat(updated.active()).isTrue();
        verify(dishes).updateActive(DISH_ID, true);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void setActiveRejectsACallerThatIsNotAnOwner(Role role) {
        assertThatThrownBy(() -> useCase.setActive(new Caller(OWNER_ID, role), DISH_ID, false))
                .isInstanceOf(AccessDenied.class);
        verifyNoInteractions(dishes, restaurants, owners);
    }

    @Test
    void setActiveRejectsAnUnknownDish() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.setActive(OWNER, DISH_ID, false)).isInstanceOf(DishNotFound.class);
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).updateActive(anyLong(), anyBoolean());
        verify(dishes, never()).save(any());
    }

    @Test
    void setActiveRejectsAnUnknownRestaurant() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.setActive(OWNER, DISH_ID, false)).isInstanceOf(RestaurantNotFound.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).updateActive(anyLong(), anyBoolean());
    }

    @Test
    void setActiveRejectsAnOwnerOfAnotherRestaurant() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));

        assertThatThrownBy(() -> useCase.setActive(new Caller(99L, Role.OWNER), DISH_ID, false))
                .isInstanceOf(AccessDenied.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).updateActive(anyLong(), anyBoolean());
    }

    @Test
    void setActiveRejectsAnUnknownOwner() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.setActive(OWNER, DISH_ID, false)).isInstanceOf(OwnerNotFound.class);
        verify(dishes, never()).updateActive(anyLong(), anyBoolean());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void setActiveRejectsAnAccountThatIsNotAnOwner(Role role) {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, role)));

        assertThatThrownBy(() -> useCase.setActive(OWNER, DISH_ID, false)).isInstanceOf(OwnerRoleRequired.class);
        verify(dishes, never()).updateActive(anyLong(), anyBoolean());
    }

    @Test
    void setActivePropagatesAnUnavailableUserService() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.setActive(OWNER, DISH_ID, false))
                .isInstanceOf(OwnerDirectoryUnavailable.class);
        verify(dishes, never()).updateActive(anyLong(), anyBoolean());
    }

    @Test
    void setActiveRejectsWhenTheRowDisappearsBeforeTheWrite() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.updateActive(DISH_ID, false)).thenReturn(false);

        assertThatThrownBy(() -> useCase.setActive(OWNER, DISH_ID, false)).isInstanceOf(DishNotFound.class);
        verify(dishes, never()).save(any());
    }

    private static Account owner() {
        return new Account(OWNER_ID, Role.OWNER);
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

    private static Dish stored(boolean active) {
        return new Dish(DISH_ID, RESTAURANT_ID, "Pasta", 12000, "Cream sauce", IMAGE, "Main", active);
    }
}

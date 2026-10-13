package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishPrice;
import com.foodcourt.plaza.domain.exception.InvalidDishText;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class UpdateDishUseCaseTest {

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

    private UpdateDishUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateDishUseCase(dishes, restaurants, owners);
    }

    @Test
    void updateReplacesPriceAndDescriptionAndKeepsTheRest() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dish updated = useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "  Tomato sauce  "));

        ArgumentCaptor<Dish> stored = ArgumentCaptor.forClass(Dish.class);
        verify(dishes).save(stored.capture());
        assertThat(stored.getValue().id()).isEqualTo(DISH_ID);
        assertThat(stored.getValue().restaurantId()).isEqualTo(RESTAURANT_ID);
        assertThat(stored.getValue().name()).isEqualTo("Pasta");
        assertThat(stored.getValue().price()).isEqualTo(15000);
        assertThat(stored.getValue().description()).isEqualTo("Tomato sauce");
        assertThat(stored.getValue().imageUrl()).isEqualTo(IMAGE);
        assertThat(stored.getValue().category()).isEqualTo("Main");
        assertThat(stored.getValue().active()).isTrue();
        assertThat(updated).isEqualTo(stored.getValue());
    }

    @Test
    void updateKeepsAnInactiveDishInactive() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(false)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dish updated = useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "Tomato sauce"));

        assertThat(updated.active()).isFalse();
        assertThat(updated.name()).isEqualTo("Pasta");
        assertThat(updated.imageUrl()).isEqualTo(IMAGE);
        assertThat(updated.category()).isEqualTo("Main");
        assertThat(updated.restaurantId()).isEqualTo(RESTAURANT_ID);
    }

    @Test
    void updateReplacesOnlyThePrice() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dish updated = useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "Cream sauce"));

        assertThat(updated.price()).isEqualTo(15000);
        assertThat(updated.description()).isEqualTo("Cream sauce");
    }

    @Test
    void updateReplacesOnlyTheDescription() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dish updated = useCase.update(OWNER, DISH_ID, new DishUpdate(12000, "Tomato sauce"));

        assertThat(updated.price()).isEqualTo(12000);
        assertThat(updated.description()).isEqualTo("Tomato sauce");
    }

    @Test
    void updateReturnsTheStoredDishWhenNothingChanged() {
        Dish current = stored(true);
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(current));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));

        Dish updated = useCase.update(OWNER, DISH_ID, new DishUpdate(12000, "  Cream sauce  "));

        assertThat(updated).isEqualTo(current);
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidPrices")
    void updateRejectsAnInvalidPriceBeforeLoadingTheDish(Integer price) {
        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(price, "Tomato sauce")))
                .isInstanceOf(InvalidDishPrice.class);
        verify(dishes, never()).findById(anyLong());
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("blankDescriptions")
    void updateRejectsABlankDescriptionBeforeLoadingTheDish(String description) {
        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(15000, description)))
                .isInstanceOf(InvalidDishText.class);
        verify(dishes, never()).findById(anyLong());
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("wordlessDescriptions")
    void updateRejectsADescriptionWithoutAWordBeforeLoadingTheDish(String description) {
        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(15000, description)))
                .isInstanceOf(InvalidDishDescription.class);
        verify(dishes, never()).findById(anyLong());
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @Test
    void updateRejectsAnUnknownDish() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "Tomato sauce")))
                .isInstanceOf(DishNotFound.class);
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @Test
    void updateRejectsAnUnknownRestaurant() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "Tomato sauce")))
                .isInstanceOf(RestaurantNotFound.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void updateRejectsACallerThatIsNotAnOwner(Role role) {
        assertThatThrownBy(() -> useCase.update(new Caller(OWNER_ID, role), DISH_ID, new DishUpdate(15000, "Tomato sauce")))
                .isInstanceOf(AccessDenied.class);
        verifyNoInteractions(dishes, restaurants, owners);
    }

    @Test
    void updateRejectsAnOwnerOfAnotherRestaurant() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));

        assertThatThrownBy(() -> useCase.update(new Caller(99L, Role.OWNER), DISH_ID, new DishUpdate(15000, "Tomato sauce")))
                .isInstanceOf(AccessDenied.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @Test
    void updateRejectsAnUnknownOwner() {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "Tomato sauce")))
                .isInstanceOf(OwnerNotFound.class);
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void updateRejectsAnAccountThatIsNotAnOwner(Role role) {
        when(dishes.findById(DISH_ID)).thenReturn(Optional.of(stored(true)));
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, role)));

        assertThatThrownBy(() -> useCase.update(OWNER, DISH_ID, new DishUpdate(15000, "Tomato sauce")))
                .isInstanceOf(OwnerRoleRequired.class);
        verify(dishes, never()).save(any());
    }

    private static Stream<Integer> invalidPrices() {
        return Stream.of(null, 0, -1);
    }

    private static Stream<String> blankDescriptions() {
        return Stream.of(null, "", "   ");
    }

    private static Stream<String> wordlessDescriptions() {
        return Stream.of("123", "!!!", "123---", "   999   ");
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

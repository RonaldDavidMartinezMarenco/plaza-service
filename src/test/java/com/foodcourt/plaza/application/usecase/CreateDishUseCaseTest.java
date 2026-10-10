package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishPrice;
import com.foodcourt.plaza.domain.exception.InvalidDishText;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class CreateDishUseCaseTest {

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

    private CreateDishUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateDishUseCase(dishes, restaurants, owners);
    }

    @Test
    void createStoresATrimmedActiveDishForThatRestaurant() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(dishes.save(any(Dish.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 21L));

        Dish created = useCase.create(OWNER, draft("  Pasta   Alfredo  ", 12000, "  Cream   sauce  ", "  Main   course  "));

        ArgumentCaptor<Dish> stored = ArgumentCaptor.forClass(Dish.class);
        verify(dishes).save(stored.capture());
        assertThat(stored.getValue().id()).isNull();
        assertThat(stored.getValue().restaurantId()).isEqualTo(RESTAURANT_ID);
        assertThat(stored.getValue().name()).isEqualTo("Pasta Alfredo");
        assertThat(stored.getValue().price()).isEqualTo(12000);
        assertThat(stored.getValue().description()).isEqualTo("Cream sauce");
        assertThat(stored.getValue().imageUrl()).isEqualTo(IMAGE);
        assertThat(stored.getValue().category()).isEqualTo("Main course");
        assertThat(stored.getValue().active()).isTrue();
        assertThat(created.id()).isEqualTo(21L);
        assertThat(created.active()).isTrue();
        verify(owners).findById(OWNER_ID);
    }

    @ParameterizedTest
    @MethodSource("invalidPrices")
    void createRejectsAnInvalidPriceBeforeLoadingTheRestaurant(Integer price) {
        assertThatThrownBy(() -> useCase.create(OWNER, draft("Pasta", price, "Cream sauce", "Main")))
                .isInstanceOf(InvalidDishPrice.class);
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidLabels")
    void createRejectsALabelOutsideTheShape(NewDish draft) {
        assertThatThrownBy(() -> useCase.create(OWNER, draft)).isInstanceOf(InvalidDishText.class);
        verify(restaurants, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("blankTexts")
    void createRejectsBlankTextBeforeLoadingTheRestaurant(NewDish draft) {
        assertThatThrownBy(() -> useCase.create(OWNER, draft)).isInstanceOf(InvalidDishText.class);
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("wordlessDescriptions")
    void createRejectsADescriptionWithoutAWordBeforeLoadingTheRestaurant(String description) {
        assertThatThrownBy(() -> useCase.create(OWNER, draft("Pasta", 12000, description, "Main")))
                .isInstanceOf(InvalidDishDescription.class);
        verify(restaurants, never()).findById(anyLong());
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @Test
    void createRejectsAnUnknownRestaurant() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(OWNER, draft("Pasta", 12000, "Cream sauce", "Main")))
                .isInstanceOf(RestaurantNotFound.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void createRejectsACallerThatIsNotAnOwner(Role role) {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));

        assertThatThrownBy(() -> useCase.create(new Caller(OWNER_ID, role), draft("Pasta", 12000, "Cream sauce", "Main")))
                .isInstanceOf(AccessDenied.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @Test
    void createRejectsAnOwnerOfAnotherRestaurant() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));

        assertThatThrownBy(() -> useCase.create(new Caller(99L, Role.OWNER), draft("Pasta", 12000, "Cream sauce", "Main")))
                .isInstanceOf(AccessDenied.class);
        verify(owners, never()).findById(anyLong());
        verify(dishes, never()).save(any());
    }

    @Test
    void createRejectsAnUnknownOwner() {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(OWNER, draft("Pasta", 12000, "Cream sauce", "Main")))
                .isInstanceOf(OwnerNotFound.class);
        verify(dishes, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void createRejectsAnAccountThatIsNotAnOwner(Role role) {
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, role)));

        assertThatThrownBy(() -> useCase.create(OWNER, draft("Pasta", 12000, "Cream sauce", "Main")))
                .isInstanceOf(OwnerRoleRequired.class);
        verify(dishes, never()).save(any());
    }

    private static Stream<Integer> invalidPrices() {
        return Stream.of(null, 0, -1);
    }

    private static Stream<String> wordlessDescriptions() {
        return Stream.of("123", "!!!", "123---", "   999   ", "a", "Hola\nMundo");
    }

    private static Stream<NewDish> invalidLabels() {
        return Stream.of(
                draft("Pasta!", 12000, "Cream sauce", "Main"),
                draft("A", 12000, "Cream sauce", "Main"),
                draft("Pasta", 12000, "Cream sauce", "***"));
    }

    private static Stream<NewDish> blankTexts() {
        return Stream.of(
                draft("   ", 12000, "Cream sauce", "Main"),
                draft("Pasta", 12000, "   ", "Main"),
                draft("Pasta", 12000, "Cream sauce", "   "));
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

    private static NewDish draft(String name, Integer price, String description, String category) {
        return new NewDish(name, price, description, IMAGE, category, RESTAURANT_ID);
    }

    private static Dish withId(Dish dish, long id) {
        return new Dish(
                id,
                dish.restaurantId(),
                dish.name(),
                dish.price(),
                dish.description(),
                dish.imageUrl(),
                dish.category(),
                dish.active());
    }
}

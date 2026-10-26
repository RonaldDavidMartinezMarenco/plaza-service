package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.InvalidDishText;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.DishListQuery;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class ListRestaurantDishesUseCaseTest {

    private static final PageQuery PAGE = new PageQuery(0, 10);
    private static final Caller CUSTOMER = new Caller(9L, Role.CUSTOMER);
    private static final DishSummary CHURRASCO = new DishSummary(
            1L,
            1L,
            "Churrasco",
            45000,
            "Corte de res de 400 g a la parrilla con papas criollas y chimichurri.",
            "https://images.example.com/dishes/churrasco.jpg",
            "Carnes");

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private DishRepository dishes;

    private ListRestaurantDishesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListRestaurantDishesUseCase(restaurants, dishes);
    }

    @Test
    void listReturnsEveryActiveDishWhenTheCategoryIsAbsent() {
        PageResult<DishSummary> page = new PageResult<>(List.of(CHURRASCO), 0, 10, 4);
        when(restaurants.findById(1L)).thenReturn(Optional.of(restaurant()));
        when(dishes.findActivePage(1L, new DishListQuery(PAGE, null))).thenReturn(page);

        PageResult<DishSummary> result = useCase.list(CUSTOMER, 1L, PAGE, null);

        assertThat(result).isEqualTo(page);
        verify(dishes).findActivePage(1L, new DishListQuery(PAGE, null));
    }

    @Test
    void listPassesTheNormalizedCategory() {
        PageResult<DishSummary> page = new PageResult<>(List.of(CHURRASCO), 0, 10, 2);
        when(restaurants.findById(1L)).thenReturn(Optional.of(restaurant()));
        when(dishes.findActivePage(1L, new DishListQuery(PAGE, "Carnes"))).thenReturn(page);

        PageResult<DishSummary> result = useCase.list(CUSTOMER, 1L, PAGE, "  Carnes  ");

        assertThat(result).isEqualTo(page);
        verify(dishes).findActivePage(1L, new DishListQuery(PAGE, "Carnes"));
    }

    @Test
    void listReturnsAnEmptyPage() {
        PageResult<DishSummary> page = new PageResult<>(List.of(), 1, 10, 2);
        when(restaurants.findById(1L)).thenReturn(Optional.of(restaurant()));
        when(dishes.findActivePage(1L, new DishListQuery(PAGE, null))).thenReturn(page);

        PageResult<DishSummary> result = useCase.list(CUSTOMER, 1L, PAGE, null);

        assertThat(result).isEqualTo(page);
        assertThat(result.items()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "EMPLOYEE"})
    void listRejectsACallerThatIsNotACustomer(Role role) {
        assertThatThrownBy(() -> useCase.list(new Caller(2L, role), 1L, PAGE, "Carnes"))
                .isInstanceOf(AccessDenied.class);
        verifyNoInteractions(restaurants, dishes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "123", "Carnes!"})
    void listRejectsACategoryOutsideTheStoredShape(String category) {
        assertThatThrownBy(() -> useCase.list(CUSTOMER, 999L, PAGE, category))
                .isInstanceOf(InvalidDishText.class);
        verifyNoInteractions(restaurants, dishes);
    }

    @Test
    void listRejectsAMissingRestaurant() {
        when(restaurants.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.list(CUSTOMER, 999L, PAGE, null))
                .isInstanceOf(RestaurantNotFound.class);
        verifyNoInteractions(dishes);
    }

    private static Restaurant restaurant() {
        return new Restaurant(
                1L,
                "La Parrilla de Carlos",
                "900100001",
                "Calle 10 # 5-20, Local 101",
                "+576015550101",
                "https://images.example.com/logos/la-parrilla.png",
                2L);
    }
}

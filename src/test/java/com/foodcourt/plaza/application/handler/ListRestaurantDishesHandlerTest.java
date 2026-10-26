package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.ListRestaurantDishes;
import com.foodcourt.plaza.domain.exception.AuthenticationRequired;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class ListRestaurantDishesHandlerTest {

    private static final Caller CUSTOMER = new Caller(9L, Role.CUSTOMER);
    private static final PageQuery PAGE = new PageQuery(0, 10);

    @Mock
    private ListRestaurantDishes listRestaurantDishes;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void listForwardsTheCallerTheRestaurantAndTheCategory() {
        PageResult<DishSummary> page = new PageResult<>(List.of(summary()), 0, 10, 2);
        when(currentCaller.require()).thenReturn(CUSTOMER);
        when(listRestaurantDishes.list(CUSTOMER, 1L, PAGE, "  Carnes  ")).thenReturn(page);
        ListRestaurantDishesHandler handler = new ListRestaurantDishesHandler(listRestaurantDishes, currentCaller);

        PageResult<DishSummary> result = handler.list(1L, PAGE, "  Carnes  ");

        assertThat(result).isEqualTo(page);
        verify(listRestaurantDishes).list(CUSTOMER, 1L, PAGE, "  Carnes  ");
    }

    @Test
    void listPropagatesAMissingCaller() {
        when(currentCaller.require()).thenThrow(new AuthenticationRequired());
        ListRestaurantDishesHandler handler = new ListRestaurantDishesHandler(listRestaurantDishes, currentCaller);

        assertThatThrownBy(() -> handler.list(1L, PAGE, null)).isInstanceOf(AuthenticationRequired.class);
        verifyNoInteractions(listRestaurantDishes);
    }

    private static DishSummary summary() {
        return new DishSummary(
                1L,
                1L,
                "Churrasco",
                45000,
                "Corte de res de 400 g a la parrilla con papas criollas y chimichurri.",
                "https://images.example.com/dishes/churrasco.jpg",
                "Carnes");
    }
}

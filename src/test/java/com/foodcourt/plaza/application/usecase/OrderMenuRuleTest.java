package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.DishInactive;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.DishRestaurantMismatch;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.spi.DishRepository;

@ExtendWith(MockitoExtension.class)
class OrderMenuRuleTest {

    private static final long RESTAURANT_ID = 1L;

    @Mock
    private DishRepository dishes;

    private OrderMenuRule rule;

    @BeforeEach
    void setUp() {
        rule = new OrderMenuRule(dishes);
    }

    @Test
    void requireOrderableAcceptsActiveDishesOfTheRestaurant() {
        when(dishes.findById(1L)).thenReturn(Optional.of(dish(1L, RESTAURANT_ID, true)));
        when(dishes.findById(4L)).thenReturn(Optional.of(dish(4L, RESTAURANT_ID, true)));

        assertThatCode(() -> rule.requireOrderable(RESTAURANT_ID, List.of(new OrderLine(1L, 1), new OrderLine(4L, 2))))
                .doesNotThrowAnyException();
    }

    @Test
    void requireOrderableRejectsAMissingDish() {
        when(dishes.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rule.requireOrderable(RESTAURANT_ID, List.of(new OrderLine(1L, 1))))
                .isInstanceOf(DishNotFound.class);
    }

    @Test
    void requireOrderableRejectsADishFromAnotherRestaurant() {
        when(dishes.findById(8L)).thenReturn(Optional.of(dish(8L, 3L, true)));

        assertThatThrownBy(() -> rule.requireOrderable(RESTAURANT_ID, List.of(new OrderLine(8L, 1))))
                .isInstanceOf(DishRestaurantMismatch.class);
    }

    @Test
    void requireOrderableRejectsADishWithoutARestaurant() {
        when(dishes.findById(1L)).thenReturn(Optional.of(dish(1L, null, true)));

        assertThatThrownBy(() -> rule.requireOrderable(RESTAURANT_ID, List.of(new OrderLine(1L, 1))))
                .isInstanceOf(DishRestaurantMismatch.class);
    }

    @Test
    void requireOrderableRejectsAnInactiveDish() {
        when(dishes.findById(2L)).thenReturn(Optional.of(dish(2L, RESTAURANT_ID, false)));

        assertThatThrownBy(() -> rule.requireOrderable(RESTAURANT_ID, List.of(new OrderLine(2L, 1))))
                .isInstanceOf(DishInactive.class);
    }

    private static Dish dish(long id, Long restaurantId, boolean active) {
        return new Dish(
                id,
                restaurantId,
                "Churrasco",
                45000,
                "Corte de res",
                "https://images.example.com/dishes/churrasco.jpg",
                "Carnes",
                active);
    }
}

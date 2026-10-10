package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.UpdateDish;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class UpdateDishHandlerTest {

    private static final Caller OWNER = new Caller(15L, Role.OWNER);

    @Mock
    private UpdateDish updateDish;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void updateForwardsTheChangeAndReturnsTheStoredDish() {
        DishUpdate change = new DishUpdate(15000, "  Tomato sauce  ");
        Dish stored = new Dish(
                21L,
                4L,
                "Pasta",
                15000,
                "Tomato sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                true);
        when(currentCaller.require()).thenReturn(OWNER);
        when(updateDish.update(OWNER, 21L, change)).thenReturn(stored);
        UpdateDishHandler handler = new UpdateDishHandler(updateDish, currentCaller);

        Dish result = handler.update(21L, change);

        assertThat(result).isEqualTo(stored);
        verify(updateDish).update(OWNER, 21L, change);
    }
}

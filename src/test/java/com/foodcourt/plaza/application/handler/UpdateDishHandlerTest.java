package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.UpdateDish;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;

@ExtendWith(MockitoExtension.class)
class UpdateDishHandlerTest {

    @Mock
    private UpdateDish updateDish;

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
        when(updateDish.update(21L, change)).thenReturn(stored);
        UpdateDishHandler handler = new UpdateDishHandler(updateDish);

        Dish result = handler.update(21L, change);

        assertThat(result).isEqualTo(stored);
        verify(updateDish).update(21L, change);
    }
}

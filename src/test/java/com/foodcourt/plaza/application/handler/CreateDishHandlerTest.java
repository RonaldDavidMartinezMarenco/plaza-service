package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.CreateDish;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;

@ExtendWith(MockitoExtension.class)
class CreateDishHandlerTest {

    @Mock
    private CreateDish createDish;

    @Test
    void createForwardsTheDishAndReturnsTheStoredOne() {
        NewDish incoming = new NewDish(
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                4L);
        Dish stored = new Dish(
                21L,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                true);
        when(createDish.create(incoming)).thenReturn(stored);
        CreateDishHandler handler = new CreateDishHandler(createDish);

        Dish result = handler.create(incoming);

        assertThat(result).isEqualTo(stored);
        verify(createDish).create(incoming);
    }
}

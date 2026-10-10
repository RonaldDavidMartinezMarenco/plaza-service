package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.CreateDish;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class CreateDishHandlerTest {

    private static final Caller OWNER = new Caller(15L, Role.OWNER);

    @Mock
    private CreateDish createDish;

    @Mock
    private CurrentCaller currentCaller;

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
        when(currentCaller.require()).thenReturn(OWNER);
        when(createDish.create(OWNER, incoming)).thenReturn(stored);
        CreateDishHandler handler = new CreateDishHandler(createDish, currentCaller);

        Dish result = handler.create(incoming);

        assertThat(result).isEqualTo(stored);
        verify(createDish).create(OWNER, incoming);
    }
}

package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.SetDishActive;
import com.foodcourt.plaza.domain.exception.AuthenticationRequired;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class SetDishActiveHandlerTest {

    private static final Caller OWNER = new Caller(15L, Role.OWNER);

    @Mock
    private SetDishActive setDishActive;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void setActiveForwardsTheCallerAndReturnsTheDish() {
        Dish stored = dish(false);
        when(currentCaller.require()).thenReturn(OWNER);
        when(setDishActive.setActive(OWNER, 21L, false)).thenReturn(stored);
        SetDishActiveHandler handler = new SetDishActiveHandler(setDishActive, currentCaller);

        Dish result = handler.setActive(21L, false);

        assertThat(result).isEqualTo(stored);
        verify(setDishActive).setActive(OWNER, 21L, false);
    }

    @Test
    void setActivePropagatesAMissingCaller() {
        when(currentCaller.require()).thenThrow(new AuthenticationRequired());
        SetDishActiveHandler handler = new SetDishActiveHandler(setDishActive, currentCaller);

        assertThatThrownBy(() -> handler.setActive(21L, false)).isInstanceOf(AuthenticationRequired.class);
        verify(setDishActive, never()).setActive(any(Caller.class), anyLong(), anyBoolean());
    }

    private static Dish dish(boolean active) {
        return new Dish(
                21L,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                active);
    }
}

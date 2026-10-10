package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.CreateRestaurant;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class CreateRestaurantHandlerTest {

    private static final Caller ADMINISTRATOR = new Caller(1L, Role.ADMINISTRATOR);

    @Mock
    private CreateRestaurant createRestaurant;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void createForwardsTheRestaurantAndReturnsTheStoredOne() {
        NewRestaurant incoming = new NewRestaurant(
                "Sushi 24",
                "900123456",
                "Calle 10",
                "+573005698325",
                "https://cdn.example.com/logos/sushi.png",
                15L);
        Restaurant stored = new Restaurant(
                8L,
                "Sushi 24",
                "900123456",
                "Calle 10",
                "+573005698325",
                "https://cdn.example.com/logos/sushi.png",
                15L);
        when(currentCaller.require()).thenReturn(ADMINISTRATOR);
        when(createRestaurant.create(ADMINISTRATOR, incoming)).thenReturn(stored);
        CreateRestaurantHandler handler = new CreateRestaurantHandler(createRestaurant, currentCaller);

        Restaurant result = handler.create(incoming);

        assertThat(result).isEqualTo(stored);
        verify(createRestaurant).create(ADMINISTRATOR, incoming);
    }
}

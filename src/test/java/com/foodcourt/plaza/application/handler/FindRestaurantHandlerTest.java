package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.FindRestaurant;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class FindRestaurantHandlerTest {

    private static final Caller OWNER = new Caller(15L, Role.OWNER);

    @Mock
    private FindRestaurant findRestaurant;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void findForwardsTheCallerAndTheId() {
        Restaurant stored =
                new Restaurant(8L, "Sushi 24", "900123456", "Calle 10", "+573005698325", "https://cdn.example.com/s.png", 15L);
        when(currentCaller.require()).thenReturn(OWNER);
        when(findRestaurant.find(OWNER, 8L)).thenReturn(stored);
        FindRestaurantHandler handler = new FindRestaurantHandler(findRestaurant, currentCaller);

        Restaurant result = handler.find(8L);

        assertThat(result).isEqualTo(stored);
        verify(findRestaurant).find(OWNER, 8L);
    }
}

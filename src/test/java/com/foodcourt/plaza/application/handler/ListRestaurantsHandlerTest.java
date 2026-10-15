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

import com.foodcourt.plaza.domain.api.ListRestaurants;
import com.foodcourt.plaza.domain.exception.AuthenticationRequired;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class ListRestaurantsHandlerTest {

    private static final Caller CUSTOMER = new Caller(9L, Role.CUSTOMER);
    private static final PageQuery QUERY = new PageQuery(0, 10);

    @Mock
    private ListRestaurants listRestaurants;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void listForwardsTheCallerAndTheQuery() {
        PageResult<RestaurantSummary> page = new PageResult<>(
                List.of(new RestaurantSummary(8L, "7 Mares Cevichería", "https://images.example.com/logos/7-mares.png")),
                0,
                10,
                23);
        when(currentCaller.require()).thenReturn(CUSTOMER);
        when(listRestaurants.list(CUSTOMER, QUERY)).thenReturn(page);
        ListRestaurantsHandler handler = new ListRestaurantsHandler(listRestaurants, currentCaller);

        PageResult<RestaurantSummary> result = handler.list(QUERY);

        assertThat(result).isEqualTo(page);
        verify(listRestaurants).list(CUSTOMER, QUERY);
    }

    @Test
    void listPropagatesAMissingCaller() {
        when(currentCaller.require()).thenThrow(new AuthenticationRequired());
        ListRestaurantsHandler handler = new ListRestaurantsHandler(listRestaurants, currentCaller);

        assertThatThrownBy(() -> handler.list(QUERY)).isInstanceOf(AuthenticationRequired.class);
        verifyNoInteractions(listRestaurants);
    }
}

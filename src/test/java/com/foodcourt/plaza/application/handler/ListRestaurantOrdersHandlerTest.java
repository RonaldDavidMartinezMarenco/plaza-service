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

import com.foodcourt.plaza.domain.api.ListRestaurantOrders;
import com.foodcourt.plaza.domain.exception.AuthenticationRequired;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class ListRestaurantOrdersHandlerTest {

    private static final Caller EMPLOYEE = new Caller(5L, Role.EMPLOYEE);
    private static final OrderListQuery QUERY = new OrderListQuery(new PageQuery(0, 10), OrderStatus.PENDING);

    @Mock
    private ListRestaurantOrders listRestaurantOrders;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void listForwardsTheCallerAndTheQuery() {
        PageResult<Order> page = new PageResult<>(
                List.of(new Order(1L, 9L, 1L, OrderStatus.PENDING, null, null, List.of(new OrderLine(1L, 1)))),
                0,
                10,
                12);
        when(currentCaller.require()).thenReturn(EMPLOYEE);
        when(listRestaurantOrders.list(EMPLOYEE, QUERY)).thenReturn(page);
        ListRestaurantOrdersHandler handler = new ListRestaurantOrdersHandler(listRestaurantOrders, currentCaller);

        PageResult<Order> result = handler.list(QUERY);

        assertThat(result).isEqualTo(page);
        verify(listRestaurantOrders).list(EMPLOYEE, QUERY);
    }

    @Test
    void listPropagatesAMissingCaller() {
        when(currentCaller.require()).thenThrow(new AuthenticationRequired());
        ListRestaurantOrdersHandler handler = new ListRestaurantOrdersHandler(listRestaurantOrders, currentCaller);

        assertThatThrownBy(() -> handler.list(QUERY)).isInstanceOf(AuthenticationRequired.class);
        verifyNoInteractions(listRestaurantOrders);
    }
}

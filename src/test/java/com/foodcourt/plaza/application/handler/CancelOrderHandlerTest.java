package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.CancelOrder;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class CancelOrderHandlerTest {

    private static final Caller LUCIA = new Caller(12L, Role.CUSTOMER);

    @Mock
    private CancelOrder cancelOrder;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void cancelForwardsTheCallerAndTheId() {
        Order cancelled = new Order(2L, 12L, 1L, OrderStatus.CANCELLED, null, null, List.of(new OrderLine(1L, 2)));
        when(currentCaller.require()).thenReturn(LUCIA);
        when(cancelOrder.cancel(LUCIA, 2L)).thenReturn(cancelled);
        CancelOrderHandler handler = new CancelOrderHandler(cancelOrder, currentCaller);

        assertThat(handler.cancel(2L)).isEqualTo(cancelled);
        verify(currentCaller).require();
        verify(cancelOrder).cancel(LUCIA, 2L);
    }
}

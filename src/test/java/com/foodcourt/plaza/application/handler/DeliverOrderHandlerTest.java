package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.DeliverOrder;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class DeliverOrderHandlerTest {

    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);

    @Mock
    private DeliverOrder deliverOrder;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void deliverForwardsTheCallerTheIdAndThePin() {
        Order delivered = new Order(1L, 9L, 1L, OrderStatus.DELIVERED, 5L, "hash", List.of(new OrderLine(1L, 1)));
        when(currentCaller.require()).thenReturn(PEDRO);
        when(deliverOrder.deliver(PEDRO, 1L, "048215")).thenReturn(delivered);
        DeliverOrderHandler handler = new DeliverOrderHandler(deliverOrder, currentCaller);

        assertThat(handler.deliver(1L, "048215")).isEqualTo(delivered);
        verify(deliverOrder).deliver(PEDRO, 1L, "048215");
    }
}

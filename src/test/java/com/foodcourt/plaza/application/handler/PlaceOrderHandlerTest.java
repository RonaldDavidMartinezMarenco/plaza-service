package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.PlaceOrder;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft.OrderLineDraft;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class PlaceOrderHandlerTest {

    private static final Caller CUSTOMER = new Caller(9L, Role.CUSTOMER);

    @Mock
    private PlaceOrder placeOrder;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void placeForwardsTheDraftAndReturnsTheStoredOrder() {
        PlaceOrderDraft incoming = new PlaceOrderDraft(1L, List.of(new OrderLineDraft(1L, 1)));
        Order stored = new Order(3L, 9L, 1L, OrderStatus.PENDING, null, null, List.of(new OrderLine(1L, 1)));
        when(currentCaller.require()).thenReturn(CUSTOMER);
        when(placeOrder.place(CUSTOMER, incoming)).thenReturn(stored);
        PlaceOrderHandler handler = new PlaceOrderHandler(placeOrder, currentCaller);

        Order result = handler.place(incoming);

        assertThat(result).isEqualTo(stored);
        verify(placeOrder).place(CUSTOMER, incoming);
    }
}

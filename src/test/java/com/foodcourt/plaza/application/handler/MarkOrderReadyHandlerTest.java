package com.foodcourt.plaza.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.api.MarkOrderReady;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class MarkOrderReadyHandlerTest {

    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);

    @Mock
    private MarkOrderReady markOrderReady;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void markReadyForwardsTheCallerAndTheId() {
        Order ready = new Order(1L, 9L, 1L, OrderStatus.READY, 5L, "hash", List.of(new OrderLine(1L, 1)));
        when(currentCaller.require()).thenReturn(PEDRO);
        when(markOrderReady.markReady(PEDRO, 1L)).thenReturn(ready);
        MarkOrderReadyHandler handler = new MarkOrderReadyHandler(markOrderReady, currentCaller);

        assertThat(handler.markReady(1L)).isEqualTo(ready);
        verify(markOrderReady).markReady(PEDRO, 1L);
    }
}

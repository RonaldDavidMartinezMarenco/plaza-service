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

import com.foodcourt.plaza.domain.api.AssignOrder;
import com.foodcourt.plaza.domain.exception.AuthenticationRequired;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

@ExtendWith(MockitoExtension.class)
class AssignOrderHandlerTest {

    private static final Caller EMPLOYEE = new Caller(5L, Role.EMPLOYEE);

    @Mock
    private AssignOrder assignOrder;

    @Mock
    private CurrentCaller currentCaller;

    @Test
    void assignForwardsTheCallerAndTheOrderId() {
        Order claimed = new Order(1L, 9L, 1L, OrderStatus.IN_PREPARATION, 5L, null, List.of(new OrderLine(1L, 1)));
        when(currentCaller.require()).thenReturn(EMPLOYEE);
        when(assignOrder.assign(EMPLOYEE, 1L)).thenReturn(claimed);
        AssignOrderHandler handler = new AssignOrderHandler(assignOrder, currentCaller);

        Order result = handler.assign(1L);

        assertThat(result).isEqualTo(claimed);
        verify(assignOrder).assign(EMPLOYEE, 1L);
    }

    @Test
    void assignPropagatesAMissingCaller() {
        when(currentCaller.require()).thenThrow(new AuthenticationRequired());
        AssignOrderHandler handler = new AssignOrderHandler(assignOrder, currentCaller);

        assertThatThrownBy(() -> handler.assign(1L)).isInstanceOf(AuthenticationRequired.class);
        verifyNoInteractions(assignOrder);
    }
}

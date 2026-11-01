package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OrderTest {

    @Test
    void placeStartsPendingWithoutAnEmployeeOrPin() {
        Order order = Order.place(9L, 1L, List.of(new OrderLine(1L, 1), new OrderLine(4L, 2)));

        assertThat(order.id()).isNull();
        assertThat(order.customerId()).isEqualTo(9L);
        assertThat(order.restaurantId()).isEqualTo(1L);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.assignedEmployeeId()).isNull();
        assertThat(order.pinHash()).isNull();
        assertThat(order.lines()).containsExactly(new OrderLine(1L, 1), new OrderLine(4L, 2));
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "IN_PREPARATION", "READY"})
    void inProgressKeepsTheCustomerFromOrderingAgain(OrderStatus status) {
        assertThat(OrderStatus.inProgress()).contains(status);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
    void aFinishedOrderDoesNotBlockAnother(OrderStatus status) {
        assertThat(OrderStatus.inProgress()).doesNotContain(status);
    }
}

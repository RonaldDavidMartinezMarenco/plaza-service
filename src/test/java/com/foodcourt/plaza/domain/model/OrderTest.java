package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;

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

    @Test
    void assignMovesAPendingOrderToPreparation() {
        Order order = new Order(
                1L, 9L, 1L, OrderStatus.PENDING, null, null, List.of(new OrderLine(1L, 1), new OrderLine(4L, 2)));

        Order assigned = order.assign(5L);

        assertThat(assigned.id()).isEqualTo(1L);
        assertThat(assigned.customerId()).isEqualTo(9L);
        assertThat(assigned.restaurantId()).isEqualTo(1L);
        assertThat(assigned.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(assigned.assignedEmployeeId()).isEqualTo(5L);
        assertThat(assigned.pinHash()).isNull();
        assertThat(assigned.lines()).containsExactly(new OrderLine(1L, 1), new OrderLine(4L, 2));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.assignedEmployeeId()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "READY", "DELIVERED", "CANCELLED"})
    void assignRejectsAnyStatusOtherThanPending(OrderStatus status) {
        Order order = new Order(1L, 9L, 1L, status, null, null, List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.assign(5L))
                .isInstanceOf(OrderNotAssignable.class)
                .hasMessage("The order is " + status + " and cannot be assigned");
        assertThat(order.status()).isEqualTo(status);
        assertThat(order.assignedEmployeeId()).isNull();
    }

    @Test
    void assignRejectsAPendingOrderThatAlreadyHasAnEmployee() {
        Order order = new Order(1L, 9L, 1L, OrderStatus.PENDING, 6L, null, List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.assign(5L))
                .isInstanceOf(OrderAlreadyAssigned.class)
                .hasMessage("The order already has an assigned employee");
        assertThat(order.assignedEmployeeId()).isEqualTo(6L);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }
}

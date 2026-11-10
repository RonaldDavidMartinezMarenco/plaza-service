package com.foodcourt.plaza.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderAssignedToAnotherEmployee;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;
import com.foodcourt.plaza.domain.exception.OrderNotCancellable;
import com.foodcourt.plaza.domain.exception.OrderNotDeliverable;
import com.foodcourt.plaza.domain.exception.OrderNotReady;

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

    @Test
    void markReadyMovesPreparationToReadyWithoutAPin() {
        Order order = new Order(1L, 9L, 1L, OrderStatus.IN_PREPARATION, 5L, null, List.of(new OrderLine(1L, 1)));

        Order ready = order.markReady(5L).withPin("hash");

        assertThat(ready.status()).isEqualTo(OrderStatus.READY);
        assertThat(ready.assignedEmployeeId()).isEqualTo(5L);
        assertThat(ready.pinHash()).isEqualTo("hash");
        assertThat(order.pinHash()).isNull();
        assertThat(order.status()).isEqualTo(OrderStatus.IN_PREPARATION);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "READY", "DELIVERED", "CANCELLED"})
    void markReadyRejectsAnyStatusOtherThanPreparation(OrderStatus status) {
        Order order = new Order(1L, 9L, 1L, status, 5L, null, List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.markReady(5L))
                .isInstanceOf(OrderNotReady.class)
                .hasMessage("The order is " + status + " and cannot be marked ready");
    }

    @Test
    void markReadyRejectsAnotherEmployee() {
        Order order = new Order(1L, 9L, 1L, OrderStatus.IN_PREPARATION, 6L, null, List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.markReady(5L)).isInstanceOf(OrderAssignedToAnotherEmployee.class);
    }

    @Test
    void deliverMovesAReadyOrderToDeliveredAndKeepsThePin() {
        List<OrderLine> lines = List.of(new OrderLine(1L, 1), new OrderLine(4L, 2));
        Order order = new Order(1L, 9L, 1L, OrderStatus.READY, 5L, "hash", lines);

        Order delivered = order.deliver(5L);

        assertThat(delivered.id()).isEqualTo(1L);
        assertThat(delivered.customerId()).isEqualTo(9L);
        assertThat(delivered.restaurantId()).isEqualTo(1L);
        assertThat(delivered.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.assignedEmployeeId()).isEqualTo(5L);
        assertThat(delivered.pinHash()).isEqualTo("hash");
        assertThat(delivered.lines()).containsExactlyElementsOf(lines);
        assertThat(order.status()).isEqualTo(OrderStatus.READY);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "IN_PREPARATION", "DELIVERED", "CANCELLED"})
    void deliverRejectsAnyStatusOtherThanReady(OrderStatus status) {
        Order order = new Order(1L, 9L, 1L, status, 5L, "hash", List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.deliver(5L))
                .isInstanceOf(OrderNotDeliverable.class)
                .hasMessage("The order is " + status + " and cannot be delivered");
        assertThat(order.status()).isEqualTo(status);
    }

    @Test
    void deliverRejectsAnotherEmployee() {
        Order order = new Order(1L, 9L, 1L, OrderStatus.READY, 6L, "hash", List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.deliver(5L))
                .isInstanceOf(OrderAssignedToAnotherEmployee.class)
                .hasMessage("The order is assigned to another employee");
        assertThat(order.status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void deliverRejectsAnOrderWithoutAnAssignee() {
        Order order = new Order(1L, 9L, 1L, OrderStatus.READY, null, "hash", List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.deliver(5L)).isInstanceOf(OrderAssignedToAnotherEmployee.class);
        assertThat(order.assignedEmployeeId()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void deliverRejectsAReadyOrderWithoutAStoredPin(String pinHash) {
        Order order = new Order(1L, 9L, 1L, OrderStatus.READY, 5L, pinHash, List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.deliver(5L))
                .isInstanceOf(OrderNotDeliverable.class)
                .hasMessage("The order is READY and cannot be delivered");
    }

    @Test
    void deliverRejectsAReadyOrderWhosePinHashIsNull() {
        Order order = new Order(1L, 9L, 1L, OrderStatus.READY, 5L, null, List.of(new OrderLine(1L, 1)));

        assertThatThrownBy(() -> order.deliver(5L)).isInstanceOf(OrderNotDeliverable.class);
        assertThat(order.pinHash()).isNull();
    }

    @Test
    void cancelMovesAPendingOrderToCancelled() {
        Order order = new Order(
                2L, 12L, 1L, OrderStatus.PENDING, null, null, List.of(new OrderLine(1L, 2)));

        Order cancelled = order.cancel();

        assertThat(cancelled.id()).isEqualTo(2L);
        assertThat(cancelled.customerId()).isEqualTo(12L);
        assertThat(cancelled.restaurantId()).isEqualTo(1L);
        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.assignedEmployeeId()).isNull();
        assertThat(cancelled.pinHash()).isNull();
        assertThat(cancelled.lines()).containsExactly(new OrderLine(1L, 2));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void cancelKeepsTheStoredEmployeeAndPin() {
        Order order = new Order(2L, 12L, 1L, OrderStatus.PENDING, 5L, "hash", List.of(new OrderLine(1L, 2)));

        Order cancelled = order.cancel();

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.assignedEmployeeId()).isEqualTo(5L);
        assertThat(cancelled.pinHash()).isEqualTo("hash");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.assignedEmployeeId()).isEqualTo(5L);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "READY", "DELIVERED", "CANCELLED"})
    void cancelRejectsAnyStatusOtherThanPending(OrderStatus status) {
        Order order = new Order(2L, 12L, 1L, status, null, null, List.of(new OrderLine(1L, 2)));

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderNotCancellable.class)
                .hasMessage("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse");
        assertThat(order.status()).isEqualTo(status);
    }
}

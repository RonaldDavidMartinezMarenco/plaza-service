package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.CustomerRoleRequired;
import com.foodcourt.plaza.domain.exception.OrderNotCancellable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {

    private static final Caller LUCIA = new Caller(12L, Role.CUSTOMER);
    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);
    private static final List<OrderLine> LINES = List.of(new OrderLine(1L, 2));
    private static final String NOT_PENDING =
            "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";

    @Mock
    private OrderRepository orders;

    @Mock
    private OwnerDirectory owners;

    private CancelOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelOrderUseCase(orders, owners);
    }

    @Test
    void cancelStoresTheCustomersPendingOrder() {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(2L, LUCIA.id())).thenReturn(Optional.of(pending()));
        when(orders.cancelIfPending(2L, LUCIA.id())).thenReturn(true);

        Order cancelled = useCase.cancel(LUCIA, 2L);

        assertThat(cancelled.id()).isEqualTo(2L);
        assertThat(cancelled.customerId()).isEqualTo(LUCIA.id());
        assertThat(cancelled.restaurantId()).isEqualTo(1L);
        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.assignedEmployeeId()).isNull();
        assertThat(cancelled.pinHash()).isNull();
        assertThat(cancelled.lines()).containsExactlyElementsOf(LINES);
        verify(orders).cancelIfPending(2L, LUCIA.id());
    }

    @Test
    void cancelKeepsAPendingEmployeeAndPinWhenTheUpdateWins() {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(2L, LUCIA.id()))
                .thenReturn(Optional.of(new Order(2L, LUCIA.id(), 1L, OrderStatus.PENDING, 5L, "hash", LINES)));
        when(orders.cancelIfPending(2L, LUCIA.id())).thenReturn(true);

        Order cancelled = useCase.cancel(LUCIA, 2L);

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.assignedEmployeeId()).isEqualTo(5L);
        assertThat(cancelled.pinHash()).isEqualTo("hash");
        verify(orders).cancelIfPending(2L, LUCIA.id());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "EMPLOYEE"})
    void cancelRejectsACallerThatIsNotACustomer(Role role) {
        assertThatThrownBy(() -> useCase.cancel(new Caller(12L, role), 2L)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(owners, orders);
    }

    @Test
    void cancelRejectsAMissingCustomerAccount() {
        when(owners.findById(LUCIA.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L)).isInstanceOf(CustomerNotFound.class);
        verifyNoInteractions(orders);
    }

    @Test
    void cancelRejectsAnAccountThatIsNoLongerACustomer() {
        when(owners.findById(LUCIA.id())).thenReturn(Optional.of(new Account(LUCIA.id(), Role.EMPLOYEE, 1L)));

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L)).isInstanceOf(CustomerRoleRequired.class);
        verifyNoInteractions(orders);
    }

    @Test
    void cancelPropagatesADirectoryFailure() {
        when(owners.findById(LUCIA.id())).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L)).isInstanceOf(OwnerDirectoryUnavailable.class);
        verifyNoInteractions(orders);
    }

    @Test
    void cancelHidesAnOrderThatBelongsToAnotherCustomer() {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(1L, LUCIA.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 1L))
                .isInstanceOf(OrderNotFound.class)
                .hasMessage("No order exists for this id");
        verify(orders, never()).cancelIfPending(1L, LUCIA.id());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "READY", "DELIVERED", "CANCELLED"})
    void cancelRejectsAnyStatusOtherThanPending(OrderStatus status) {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(2L, LUCIA.id())).thenReturn(Optional.of(order(status)));

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L))
                .isInstanceOf(OrderNotCancellable.class)
                .hasMessage(NOT_PENDING);
        verify(orders, never()).cancelIfPending(2L, LUCIA.id());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "READY", "DELIVERED", "CANCELLED"})
    void cancelReportsTheStatusStoredByAConcurrentChange(OrderStatus status) {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(2L, LUCIA.id()))
                .thenReturn(Optional.of(pending()), Optional.of(order(status)));
        when(orders.cancelIfPending(2L, LUCIA.id())).thenReturn(false);

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L))
                .isInstanceOf(OrderNotCancellable.class)
                .hasMessage(NOT_PENDING);
        verify(orders).cancelIfPending(2L, LUCIA.id());
    }

    @Test
    void cancelReportsAMissingOrderWhenTheRowDisappears() {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(2L, LUCIA.id())).thenReturn(Optional.of(pending()), Optional.empty());
        when(orders.cancelIfPending(2L, LUCIA.id())).thenReturn(false);

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L)).isInstanceOf(OrderNotFound.class);
        verify(orders).cancelIfPending(2L, LUCIA.id());
    }

    @Test
    void cancelReportsAConflictWhenTheUpdateMatchesNothing() {
        customer(LUCIA);
        when(orders.findByIdAndCustomerId(2L, LUCIA.id())).thenReturn(Optional.of(pending()), Optional.of(pending()));
        when(orders.cancelIfPending(2L, LUCIA.id())).thenReturn(false);

        assertThatThrownBy(() -> useCase.cancel(LUCIA, 2L))
                .isInstanceOf(OrderNotCancellable.class)
                .hasMessage("The order could not be cancelled");
        verify(orders).cancelIfPending(2L, LUCIA.id());
    }

    @Test
    void cancelDoesNotUseAnEmployeeCaller() {
        assertThatThrownBy(() -> useCase.cancel(PEDRO, 2L)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(orders);
    }

    private void customer(Caller caller) {
        when(owners.findById(caller.id())).thenReturn(Optional.of(new Account(caller.id(), Role.CUSTOMER, null)));
    }

    private static Order pending() {
        return order(OrderStatus.PENDING);
    }

    private static Order order(OrderStatus status) {
        return new Order(2L, LUCIA.id(), 1L, status, null, null, LINES);
    }
}

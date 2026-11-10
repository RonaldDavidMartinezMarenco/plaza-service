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
import com.foodcourt.plaza.domain.exception.EmployeeNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeRoleRequired;
import com.foodcourt.plaza.domain.exception.EmployeeWithoutRestaurant;
import com.foodcourt.plaza.domain.exception.OrderAssignedToAnotherEmployee;
import com.foodcourt.plaza.domain.exception.OrderNotDeliverable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.PinDoesNotMatch;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.PinHasher;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class DeliverOrderUseCaseTest {

    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);
    private static final Caller SOFIA = new Caller(6L, Role.EMPLOYEE);
    private static final String PIN = "048215";
    private static final String HASH = "$2a$10$hash";
    private static final List<OrderLine> LINES = List.of(new OrderLine(1L, 1), new OrderLine(4L, 2));

    @Mock
    private OrderRepository orders;

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private OwnerDirectory owners;

    @Mock
    private PinHasher hasher;

    private DeliverOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeliverOrderUseCase(orders, restaurants, owners, hasher);
    }

    @Test
    void deliverStoresDeliveredAndDoesNotTouchTheOtherTransitions() {
        ready(PEDRO);
        when(hasher.matches(PIN, HASH)).thenReturn(true);
        when(orders.deliverIfReady(1L, 1L, PEDRO.id(), HASH)).thenReturn(true);

        Order delivered = useCase.deliver(PEDRO, 1L, PIN);

        assertThat(delivered.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.assignedEmployeeId()).isEqualTo(PEDRO.id());
        assertThat(delivered.pinHash()).isEqualTo(HASH);
        assertThat(delivered.lines()).containsExactlyElementsOf(LINES);
        verify(orders).deliverIfReady(1L, 1L, PEDRO.id(), HASH);
        verify(orders, never()).save(org.mockito.ArgumentMatchers.any());
        verify(orders, never()).assignIfPending(1L, 1L, PEDRO.id());
        verify(orders, never()).markReadyIfInPreparation(1L, 1L, PEDRO.id(), HASH);
        verify(orders, never()).revertReady(1L, 1L, PEDRO.id(), HASH);
    }

    @Test
    void deliverRejectsAPinThatDoesNotMatchAndDoesNotWrite() {
        ready(PEDRO);
        when(hasher.matches("000000", HASH)).thenReturn(false);

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, "000000"))
                .isInstanceOf(PinDoesNotMatch.class)
                .hasMessage("The security PIN does not match");
        verify(orders, never()).deliverIfReady(1L, 1L, PEDRO.id(), HASH);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "IN_PREPARATION", "DELIVERED", "CANCELLED"})
    void deliverRejectsAnyStatusOtherThanReady(OrderStatus status) {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(status, PEDRO.id(), HASH)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN))
                .isInstanceOf(OrderNotDeliverable.class)
                .hasMessage("The order is " + status + " and cannot be delivered");
        verifyNoInteractions(hasher);
        verify(orders, never()).deliverIfReady(1L, 1L, PEDRO.id(), HASH);
    }

    @Test
    void deliverRejectsAnotherEmployeeEvenWhenThePinMatches() {
        employee(SOFIA, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.READY, PEDRO.id(), HASH)));

        assertThatThrownBy(() -> useCase.deliver(SOFIA, 1L, PIN))
                .isInstanceOf(OrderAssignedToAnotherEmployee.class)
                .hasMessage("The order is assigned to another employee");
        verifyNoInteractions(hasher);
    }

    @Test
    void deliverRejectsAnOrderWithoutAnAssignee() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.READY, null, HASH)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN)).isInstanceOf(OrderAssignedToAnotherEmployee.class);
        verifyNoInteractions(hasher);
    }

    @Test
    void deliverRejectsAReadyOrderWithoutAPinHash() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.READY, PEDRO.id(), null)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN))
                .isInstanceOf(OrderNotDeliverable.class)
                .hasMessage("The order is READY and cannot be delivered");
        verifyNoInteractions(hasher);
    }

    @Test
    void deliverHidesAnOrderFromAnotherRestaurant() {
        employee(new Caller(7L, Role.EMPLOYEE), 2L);
        when(orders.findByIdAndRestaurantId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deliver(new Caller(7L, Role.EMPLOYEE), 1L, PIN)).isInstanceOf(OrderNotFound.class);
        verifyNoInteractions(hasher);
    }

    @Test
    void deliverRejectsAnUnknownOrder() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 99L, PIN))
                .isInstanceOf(OrderNotFound.class)
                .hasMessage("No order exists for this id");
        verifyNoInteractions(hasher);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "CUSTOMER"})
    void deliverRejectsACallerThatIsNotAnEmployee(Role role) {
        assertThatThrownBy(() -> useCase.deliver(new Caller(9L, role), 1L, PIN)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(owners, restaurants, orders, hasher);
    }

    @Test
    void deliverRejectsAMissingEmployeeAccount() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN)).isInstanceOf(EmployeeNotFound.class);
        verifyNoInteractions(orders, hasher);
    }

    @Test
    void deliverRejectsAnAccountThatIsNoLongerAnEmployee() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.CUSTOMER, 1L)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN)).isInstanceOf(EmployeeRoleRequired.class);
        verifyNoInteractions(orders, hasher);
    }

    @Test
    void deliverRejectsAnEmployeeWithoutARestaurant() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, null)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN)).isInstanceOf(EmployeeWithoutRestaurant.class);
        verifyNoInteractions(orders, hasher);
    }

    @Test
    void deliverRejectsARestaurantThatDoesNotExist() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, 99L)));
        when(restaurants.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN)).isInstanceOf(RestaurantNotFound.class);
        verifyNoInteractions(orders, hasher);
    }

    @Test
    void deliverPropagatesADirectoryFailure() {
        when(owners.findById(PEDRO.id())).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN)).isInstanceOf(OwnerDirectoryUnavailable.class);
        verifyNoInteractions(orders, hasher);
    }

    @Test
    void deliverReportsDeliveredWhenAnotherRequestWins() {
        ready(PEDRO);
        when(hasher.matches(PIN, HASH)).thenReturn(true);
        when(orders.deliverIfReady(1L, 1L, PEDRO.id(), HASH)).thenReturn(false);
        when(orders.findByIdAndRestaurantId(1L, 1L))
                .thenReturn(Optional.of(order(OrderStatus.READY, PEDRO.id(), HASH)))
                .thenReturn(Optional.of(order(OrderStatus.DELIVERED, PEDRO.id(), HASH)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN))
                .isInstanceOf(OrderNotDeliverable.class)
                .hasMessage("The order is DELIVERED and cannot be delivered");
    }

    @Test
    void deliverReportsReadyWhenTheRowIsStillReadyAfterALostUpdate() {
        ready(PEDRO);
        when(hasher.matches(PIN, HASH)).thenReturn(true);
        when(orders.deliverIfReady(1L, 1L, PEDRO.id(), HASH)).thenReturn(false);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.READY, PEDRO.id(), HASH)));

        assertThatThrownBy(() -> useCase.deliver(PEDRO, 1L, PIN))
                .isInstanceOf(OrderNotDeliverable.class)
                .hasMessage("The order is READY and cannot be delivered");
    }

    private void ready(Caller caller) {
        employee(caller, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.READY, caller.id(), HASH)));
    }

    private void employee(Caller caller, long restaurantId) {
        when(owners.findById(caller.id())).thenReturn(Optional.of(new Account(caller.id(), Role.EMPLOYEE, restaurantId)));
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant(restaurantId)));
    }

    private static Order order(OrderStatus status, Long employeeId, String pinHash) {
        return new Order(1L, 9L, 1L, status, employeeId, pinHash, LINES);
    }

    private static Restaurant restaurant(long id) {
        return new Restaurant(id, "La Parrilla de Carlos", "900100001", "Calle 10", "+576015550101", "https://images.example.com/a.png", 2L);
    }
}

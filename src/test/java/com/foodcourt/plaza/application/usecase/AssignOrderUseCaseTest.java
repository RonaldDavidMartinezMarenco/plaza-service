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
import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderNotAssignable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
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
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class AssignOrderUseCaseTest {

    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);
    private static final Caller SOFIA = new Caller(6L, Role.EMPLOYEE);
    private static final List<OrderLine> LINES = List.of(new OrderLine(1L, 1), new OrderLine(4L, 2));

    @Mock
    private OrderRepository orders;

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private OwnerDirectory owners;

    private AssignOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AssignOrderUseCase(orders, restaurants, owners);
    }

    @Test
    void assignStoresTheCallerOnAPendingOrder() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(pending(1L)));
        when(orders.assignIfPending(1L, 1L, PEDRO.id())).thenReturn(true);

        Order assigned = useCase.assign(PEDRO, 1L);

        assertThat(assigned.id()).isEqualTo(1L);
        assertThat(assigned.customerId()).isEqualTo(9L);
        assertThat(assigned.restaurantId()).isEqualTo(1L);
        assertThat(assigned.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(assigned.assignedEmployeeId()).isEqualTo(PEDRO.id());
        assertThat(assigned.pinHash()).isNull();
        assertThat(assigned.lines()).containsExactlyElementsOf(LINES);
        verify(orders).assignIfPending(1L, 1L, PEDRO.id());
    }

    @Test
    void assignLetsAnotherEmployeeOfTheSameRestaurantTakeADifferentOrder() {
        employee(SOFIA, 1L);
        when(orders.findByIdAndRestaurantId(2L, 1L)).thenReturn(Optional.of(pending(2L)));
        when(orders.assignIfPending(2L, 1L, SOFIA.id())).thenReturn(true);

        Order assigned = useCase.assign(SOFIA, 2L);

        assertThat(assigned.assignedEmployeeId()).isEqualTo(SOFIA.id());
        assertThat(assigned.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        verify(orders).assignIfPending(2L, 1L, SOFIA.id());
    }

    @Test
    void assignHidesAnOrderFromAnotherRestaurant() {
        employee(new Caller(7L, Role.EMPLOYEE), 2L);
        when(orders.findByIdAndRestaurantId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.assign(new Caller(7L, Role.EMPLOYEE), 1L)).isInstanceOf(OrderNotFound.class);
        verify(orders, never()).assignIfPending(1L, 2L, 7L);
    }

    @Test
    void assignRejectsAnUnknownOrder() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.assign(PEDRO, 99L))
                .isInstanceOf(OrderNotFound.class)
                .hasMessage("No order exists for this id");
        verify(orders, never()).assignIfPending(99L, 1L, PEDRO.id());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "READY", "DELIVERED", "CANCELLED"})
    void assignRejectsAnOrderThatIsNotPending(OrderStatus status) {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(1L, status, null)));

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L))
                .isInstanceOf(OrderNotAssignable.class)
                .hasMessage("The order is " + status + " and cannot be assigned");
        verify(orders, never()).assignIfPending(1L, 1L, PEDRO.id());
    }

    @Test
    void assignRejectsAPendingOrderThatAlreadyHasAnEmployee() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(1L, OrderStatus.PENDING, 6L)));

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L))
                .isInstanceOf(OrderAlreadyAssigned.class)
                .hasMessage("The order already has an assigned employee");
        verify(orders, never()).assignIfPending(1L, 1L, PEDRO.id());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "CUSTOMER"})
    void assignRejectsACallerThatIsNotAnEmployee(Role role) {
        assertThatThrownBy(() -> useCase.assign(new Caller(9L, role), 1L)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(owners, restaurants, orders);
    }

    @Test
    void assignRejectsAMissingEmployeeAccount() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L)).isInstanceOf(EmployeeNotFound.class);
        verifyNoInteractions(restaurants, orders);
    }

    @Test
    void assignRejectsAnAccountThatIsNoLongerAnEmployee() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.CUSTOMER, 1L)));

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L)).isInstanceOf(EmployeeRoleRequired.class);
        verifyNoInteractions(restaurants, orders);
    }

    @Test
    void assignRejectsAnEmployeeWithoutARestaurant() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, null)));

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L)).isInstanceOf(EmployeeWithoutRestaurant.class);
        verifyNoInteractions(restaurants, orders);
    }

    @Test
    void assignRejectsARestaurantThatDoesNotExist() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, 99L)));
        when(restaurants.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L)).isInstanceOf(RestaurantNotFound.class);
        verifyNoInteractions(orders);
    }

    @Test
    void assignPropagatesADirectoryFailure() {
        when(owners.findById(PEDRO.id())).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L)).isInstanceOf(OwnerDirectoryUnavailable.class);
        verifyNoInteractions(restaurants, orders);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PREPARATION", "READY", "DELIVERED", "CANCELLED"})
    void assignReportsTheStatusStoredByAConcurrentClaim(OrderStatus status) {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L))
                .thenReturn(Optional.of(pending(1L)), Optional.of(order(1L, status, SOFIA.id())));
        when(orders.assignIfPending(1L, 1L, PEDRO.id())).thenReturn(false);

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L))
                .isInstanceOf(OrderNotAssignable.class)
                .hasMessage("The order is " + status + " and cannot be assigned");
        verify(orders).assignIfPending(1L, 1L, PEDRO.id());
    }

    @Test
    void assignReportsAMissingOrderWhenTheRowDisappears() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(pending(1L)), Optional.empty());
        when(orders.assignIfPending(1L, 1L, PEDRO.id())).thenReturn(false);

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L)).isInstanceOf(OrderNotFound.class);
        verify(orders).assignIfPending(1L, 1L, PEDRO.id());
    }

    @Test
    void assignReportsAConflictWhenTheUpdateMatchesNothing() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(pending(1L)), Optional.of(pending(1L)));
        when(orders.assignIfPending(1L, 1L, PEDRO.id())).thenReturn(false);

        assertThatThrownBy(() -> useCase.assign(PEDRO, 1L))
                .isInstanceOf(OrderAlreadyAssigned.class)
                .hasMessage("The order already has an assigned employee");
    }

    private void employee(Caller caller, long restaurantId) {
        when(owners.findById(caller.id())).thenReturn(Optional.of(new Account(caller.id(), Role.EMPLOYEE, restaurantId)));
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant(restaurantId)));
    }

    private static Order pending(long id) {
        return order(id, OrderStatus.PENDING, null);
    }

    private static Order order(long id, OrderStatus status, Long employeeId) {
        return new Order(id, 9L, 1L, status, employeeId, null, LINES);
    }

    private static Restaurant restaurant(long id) {
        return new Restaurant(id, "La Parrilla de Carlos", "900100001", "Calle 10", "+576015550101",
                "https://images.example.com/logos/la-parrilla.png", 2L);
    }
}

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
import com.foodcourt.plaza.domain.exception.EmployeeNotFound;
import com.foodcourt.plaza.domain.exception.EmployeeRoleRequired;
import com.foodcourt.plaza.domain.exception.EmployeeWithoutRestaurant;
import com.foodcourt.plaza.domain.exception.NotificationRejected;
import com.foodcourt.plaza.domain.exception.NotificationUnavailable;
import com.foodcourt.plaza.domain.exception.OrderAssignedToAnotherEmployee;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OrderNotReady;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.CustomerPhoneDirectory;
import com.foodcourt.plaza.domain.spi.OrderReadyNotifier;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.PinGenerator;
import com.foodcourt.plaza.domain.spi.PinHasher;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class MarkOrderReadyUseCaseTest {

    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);
    private static final Caller SOFIA = new Caller(6L, Role.EMPLOYEE);
    private static final String PHONE = "+573011590640";
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
    private CustomerPhoneDirectory customers;

    @Mock
    private PinGenerator pins;

    @Mock
    private PinHasher hasher;

    @Mock
    private OrderReadyNotifier notifier;

    private MarkOrderReadyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkOrderReadyUseCase(orders, restaurants, owners, customers, pins, hasher, notifier);
    }

    @Test
    void markReadyStoresTheHashAndSendsThePinOnce() {
        preparing(PEDRO);
        when(customers.findPhone(9L)).thenReturn(PHONE);
        when(pins.generate()).thenReturn(PIN);
        when(hasher.hash(PIN)).thenReturn(HASH);
        when(orders.markReadyIfInPreparation(1L, 1L, PEDRO.id(), HASH)).thenReturn(true);

        Order ready = useCase.markReady(PEDRO, 1L);

        assertThat(ready.status()).isEqualTo(OrderStatus.READY);
        assertThat(ready.assignedEmployeeId()).isEqualTo(PEDRO.id());
        assertThat(ready.pinHash()).isEqualTo(HASH);
        assertThat(ready.lines()).containsExactlyElementsOf(LINES);
        verify(notifier).notifyReady(1L, "La Parrilla de Carlos", PHONE, PIN);
        verify(orders, never()).revertReady(1L, 1L, PEDRO.id(), HASH);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "READY", "DELIVERED", "CANCELLED"})
    void markReadyRejectsAnyStatusOtherThanPreparation(OrderStatus status) {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(status, PEDRO.id(), null)));

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L))
                .isInstanceOf(OrderNotReady.class)
                .hasMessage("The order is " + status + " and cannot be marked ready");
        verifyNoInteractions(customers, pins, hasher, notifier);
    }

    @Test
    void markReadyRejectsAnotherEmployeeOfTheSameRestaurant() {
        employee(SOFIA, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.IN_PREPARATION, PEDRO.id(), null)));

        assertThatThrownBy(() -> useCase.markReady(SOFIA, 1L))
                .isInstanceOf(OrderAssignedToAnotherEmployee.class)
                .hasMessage("The order is assigned to another employee");
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyRejectsAnOrderWithoutAnAssignee() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.IN_PREPARATION, null, null)));

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(OrderAssignedToAnotherEmployee.class);
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyRejectsAPreparationRowThatAlreadyHasAPin() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(order(OrderStatus.IN_PREPARATION, PEDRO.id(), HASH)));

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(OrderNotReady.class);
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyHidesAnOrderFromAnotherRestaurant() {
        employee(new Caller(7L, Role.EMPLOYEE), 2L);
        when(orders.findByIdAndRestaurantId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.markReady(new Caller(7L, Role.EMPLOYEE), 1L)).isInstanceOf(OrderNotFound.class);
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyRejectsAnUnknownOrder() {
        employee(PEDRO, 1L);
        when(orders.findByIdAndRestaurantId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 99L))
                .isInstanceOf(OrderNotFound.class)
                .hasMessage("No order exists for this id");
        verifyNoInteractions(notifier);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "CUSTOMER"})
    void markReadyRejectsACallerThatIsNotAnEmployee(Role role) {
        assertThatThrownBy(() -> useCase.markReady(new Caller(9L, role), 1L)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(owners, restaurants, orders, notifier);
    }

    @Test
    void markReadyRejectsAMissingEmployeeAccount() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(EmployeeNotFound.class);
        verifyNoInteractions(orders, notifier);
    }

    @Test
    void markReadyRejectsAnAccountThatIsNoLongerAnEmployee() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.CUSTOMER, 1L)));

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(EmployeeRoleRequired.class);
        verifyNoInteractions(orders, notifier);
    }

    @Test
    void markReadyRejectsAnEmployeeWithoutARestaurant() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, null)));

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(EmployeeWithoutRestaurant.class);
        verifyNoInteractions(orders, notifier);
    }

    @Test
    void markReadyRejectsARestaurantThatDoesNotExist() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, 99L)));
        when(restaurants.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(RestaurantNotFound.class);
        verifyNoInteractions(orders, notifier);
    }

    @Test
    void markReadyRejectsAMissingCustomerPhone() {
        preparing(PEDRO);
        when(customers.findPhone(9L)).thenThrow(new CustomerNotFound());

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(CustomerNotFound.class);
        verify(orders, never()).markReadyIfInPreparation(1L, 1L, PEDRO.id(), HASH);
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyPropagatesADirectoryFailure() {
        preparing(PEDRO);
        when(customers.findPhone(9L)).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(OwnerDirectoryUnavailable.class);
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyReportsTheStatusWhenAnotherRequestWins() {
        preparing(PEDRO);
        when(customers.findPhone(9L)).thenReturn(PHONE);
        when(pins.generate()).thenReturn(PIN);
        when(hasher.hash(PIN)).thenReturn(HASH);
        when(orders.markReadyIfInPreparation(1L, 1L, PEDRO.id(), HASH)).thenReturn(false);
        when(orders.findByIdAndRestaurantId(1L, 1L))
                .thenReturn(Optional.of(order(OrderStatus.IN_PREPARATION, PEDRO.id(), null)))
                .thenReturn(Optional.of(order(OrderStatus.READY, PEDRO.id(), "other")));

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L))
                .isInstanceOf(OrderNotReady.class)
                .hasMessage("The order is READY and cannot be marked ready");
        verifyNoInteractions(notifier);
    }

    @Test
    void markReadyRollsBackWhenTheMessageIsRejected() {
        preparing(PEDRO);
        when(customers.findPhone(9L)).thenReturn(PHONE);
        when(pins.generate()).thenReturn(PIN);
        when(hasher.hash(PIN)).thenReturn(HASH);
        when(orders.markReadyIfInPreparation(1L, 1L, PEDRO.id(), HASH)).thenReturn(true);
        when(orders.revertReady(1L, 1L, PEDRO.id(), HASH)).thenReturn(true);
        org.mockito.Mockito.doThrow(new NotificationRejected()).when(notifier).notifyReady(1L, "La Parrilla de Carlos", PHONE, PIN);

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(NotificationRejected.class);
        verify(orders).revertReady(1L, 1L, PEDRO.id(), HASH);
    }

    @Test
    void markReadyRollsBackWhenMessagingIsDown() {
        preparing(PEDRO);
        when(customers.findPhone(9L)).thenReturn(PHONE);
        when(pins.generate()).thenReturn(PIN);
        when(hasher.hash(PIN)).thenReturn(HASH);
        when(orders.markReadyIfInPreparation(1L, 1L, PEDRO.id(), HASH)).thenReturn(true);
        when(orders.revertReady(1L, 1L, PEDRO.id(), HASH)).thenReturn(false);
        org.mockito.Mockito.doThrow(new NotificationUnavailable()).when(notifier).notifyReady(1L, "La Parrilla de Carlos", PHONE, PIN);

        assertThatThrownBy(() -> useCase.markReady(PEDRO, 1L)).isInstanceOf(NotificationUnavailable.class);
        verify(orders).revertReady(1L, 1L, PEDRO.id(), HASH);
    }

    private void preparing(Caller caller) {
        employee(caller, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L))
                .thenReturn(Optional.of(order(OrderStatus.IN_PREPARATION, caller.id(), null)));
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

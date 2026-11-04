package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class ListRestaurantOrdersUseCaseTest {

    private static final Caller PEDRO = new Caller(5L, Role.EMPLOYEE);
    private static final Caller SOFIA = new Caller(6L, Role.EMPLOYEE);
    private static final OrderListQuery PENDING = query(OrderStatus.PENDING);

    @Mock
    private OrderRepository orders;

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private OwnerDirectory owners;

    private ListRestaurantOrdersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListRestaurantOrdersUseCase(orders, restaurants, owners);
    }

    @Test
    void listReturnsThePageOfTheEmployeeRestaurant() {
        PageResult<Order> page = page(pendingOrder(1L, null));
        employee(PEDRO, 1L);
        when(orders.findPage(1L, PENDING)).thenReturn(page);

        assertThat(useCase.list(PEDRO, PENDING)).isEqualTo(page);
        verify(orders).findPage(1L, PENDING);
    }

    @Test
    void listUsesTheSameRestaurantForEveryEmployeeOfIt() {
        PageResult<Order> page = page(pendingOrder(1L, null));
        employee(SOFIA, 1L);
        when(orders.findPage(1L, PENDING)).thenReturn(page);

        assertThat(useCase.list(SOFIA, PENDING)).isEqualTo(page);
        verify(orders).findPage(1L, PENDING);
    }

    @Test
    void listAsksOnlyForTheRestaurantOnTheAccount() {
        employee(new Caller(7L, Role.EMPLOYEE), 2L);
        when(orders.findPage(2L, PENDING)).thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        useCase.list(new Caller(7L, Role.EMPLOYEE), PENDING);

        verify(orders).findPage(2L, PENDING);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void listPassesEachStatusToTheRestaurantPage(OrderStatus status) {
        OrderListQuery query = query(status);
        employee(PEDRO, 1L);
        when(orders.findPage(1L, query)).thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        useCase.list(PEDRO, query);

        verify(orders).findPage(1L, query);
    }

    @Test
    void listKeepsAnOrderAssignedToAnotherEmployeeOfTheSameRestaurant() {
        Order assigned = new Order(3L, 9L, 1L, OrderStatus.IN_PREPARATION, 6L, null, List.of(new OrderLine(1L, 1)));
        OrderListQuery query = query(OrderStatus.IN_PREPARATION);
        PageResult<Order> page = page(assigned);
        employee(PEDRO, 1L);
        when(orders.findPage(1L, query)).thenReturn(page);

        assertThat(useCase.list(PEDRO, query).items()).containsExactly(assigned);
    }

    @Test
    void listReturnsAnEmptyPageUnchanged() {
        PageResult<Order> empty = new PageResult<>(List.of(), 2, 10, 12);
        employee(PEDRO, 1L);
        when(orders.findPage(1L, PENDING)).thenReturn(empty);

        assertThat(useCase.list(PEDRO, PENDING)).isEqualTo(empty);
        assertThat(empty.items()).isEmpty();
        assertThat(empty.totalPages()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "CUSTOMER"})
    void listRejectsACallerThatIsNotAnEmployee(Role role) {
        assertThatThrownBy(() -> useCase.list(new Caller(9L, role), PENDING)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(owners, restaurants, orders);
    }

    @Test
    void listRejectsARestaurantThatDoesNotExist() {
        when(owners.findById(PEDRO.id())).thenReturn(Optional.of(new Account(PEDRO.id(), Role.EMPLOYEE, 99L)));
        when(restaurants.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.list(PEDRO, PENDING)).isInstanceOf(RestaurantNotFound.class);
        verifyNoInteractions(orders);
    }

    @Test
    void listPropagatesADirectoryFailure() {
        when(owners.findById(PEDRO.id())).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.list(PEDRO, PENDING)).isInstanceOf(OwnerDirectoryUnavailable.class);
        verifyNoInteractions(restaurants, orders);
    }

    private void employee(Caller caller, long restaurantId) {
        when(owners.findById(caller.id())).thenReturn(Optional.of(new Account(caller.id(), Role.EMPLOYEE, restaurantId)));
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant(restaurantId)));
    }

    private static OrderListQuery query(OrderStatus status) {
        return new OrderListQuery(new PageQuery(0, 10), status);
    }

    private static PageResult<Order> page(Order order) {
        return new PageResult<>(List.of(order), 0, 10, 1);
    }

    private static Order pendingOrder(long id, Long assignedEmployeeId) {
        return new Order(id, 9L, 1L, OrderStatus.PENDING, assignedEmployeeId, null, List.of(new OrderLine(1L, 1)));
    }

    private static Restaurant restaurant(long id) {
        return new Restaurant(id, "La Parrilla de Carlos", "900100001", "Calle 10", "+576015550101",
                "https://images.example.com/logos/la-parrilla.png", 2L);
    }
}

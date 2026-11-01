package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.exception.CustomerNotFound;
import com.foodcourt.plaza.domain.exception.CustomerRoleRequired;
import com.foodcourt.plaza.domain.exception.DishInactive;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.DishRestaurantMismatch;
import com.foodcourt.plaza.domain.exception.DuplicateOrderLine;
import com.foodcourt.plaza.domain.exception.EmptyOrder;
import com.foodcourt.plaza.domain.exception.InvalidOrderQuantity;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft.OrderLineDraft;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    private static final long CUSTOMER_ID = 9L;
    private static final long RESTAURANT_ID = 1L;
    private static final Caller CUSTOMER = new Caller(CUSTOMER_ID, Role.CUSTOMER);

    @Mock
    private OrderRepository orders;

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private DishRepository dishes;

    @Mock
    private OwnerDirectory owners;

    private PlaceOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PlaceOrderUseCase(orders, restaurants, dishes, owners);
    }

    @Test
    void placeStoresAPendingOrderForTheCustomer() {
        readyMenu();
        when(orders.hasActive(CUSTOMER_ID)).thenReturn(false);
        when(orders.save(any(Order.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 7L));

        Order placed = useCase.place(CUSTOMER, draft(line(1L, 1), line(4L, 2)));

        ArgumentCaptor<Order> stored = ArgumentCaptor.forClass(Order.class);
        verify(orders).save(stored.capture());
        assertThat(stored.getValue().id()).isNull();
        assertThat(stored.getValue().customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(stored.getValue().restaurantId()).isEqualTo(RESTAURANT_ID);
        assertThat(stored.getValue().status()).isEqualTo(OrderStatus.PENDING);
        assertThat(stored.getValue().assignedEmployeeId()).isNull();
        assertThat(stored.getValue().pinHash()).isNull();
        assertThat(stored.getValue().lines()).containsExactly(new OrderLine(1L, 1), new OrderLine(4L, 2));
        assertThat(placed.id()).isEqualTo(7L);
        assertThat(placed.status()).isEqualTo(OrderStatus.PENDING);
        verify(owners).findById(CUSTOMER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "EMPLOYEE"})
    void placeRejectsACallerThatIsNotACustomerBeforeAnyPort(Role role) {
        assertThatThrownBy(() -> useCase.place(new Caller(CUSTOMER_ID, role), draft(line(1L, 1))))
                .isInstanceOf(AccessDenied.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @Test
    void placeRejectsAnEmptyOrderBeforeAnyPort() {
        assertThatThrownBy(() -> useCase.place(CUSTOMER, new PlaceOrderDraft(RESTAURANT_ID, List.of())))
                .isInstanceOf(EmptyOrder.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @Test
    void placeRejectsAMissingLineBeforeAnyPort() {
        assertThatThrownBy(() -> useCase.place(CUSTOMER, new PlaceOrderDraft(RESTAURANT_ID, null)))
                .isInstanceOf(EmptyOrder.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @Test
    void placeRejectsANullLineBeforeAnyPort() {
        PlaceOrderDraft draft = new PlaceOrderDraft(RESTAURANT_ID, Arrays.asList((OrderLineDraft) null));

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft)).isInstanceOf(EmptyOrder.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @Test
    void placeRejectsALineWithoutADishBeforeAnyPort() {
        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(new OrderLineDraft(null, 1))))
                .isInstanceOf(EmptyOrder.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @Test
    void placeRejectsARepeatedDishBeforeAnyPort() {
        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1), line(1L, 2))))
                .isInstanceOf(DuplicateOrderLine.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @ParameterizedTest
    @EnumSource(value = OrderQuantityCase.class)
    void placeRejectsAQuantityOutsideTheRangeBeforeAnyPort(OrderQuantityCase quantity) {
        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, quantity.value))))
                .isInstanceOf(InvalidOrderQuantity.class);
        verifyNoInteractions(orders, restaurants, dishes, owners);
    }

    @Test
    void placeRejectsAMissingCustomerAccount() {
        when(owners.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1)))).isInstanceOf(CustomerNotFound.class);
        verify(restaurants, never()).findById(anyLong());
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsAnAccountThatIsNoLongerACustomer() {
        when(owners.findById(CUSTOMER_ID)).thenReturn(Optional.of(new Account(CUSTOMER_ID, Role.OWNER)));

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1)))).isInstanceOf(CustomerRoleRequired.class);
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsWhenTheUserServiceIsDown() {
        when(owners.findById(CUSTOMER_ID)).thenThrow(new OwnerDirectoryUnavailable());

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1))))
                .isInstanceOf(OwnerDirectoryUnavailable.class);
        verify(restaurants, never()).findById(anyLong());
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsAMissingRestaurant() {
        customerAccount();

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1)))).isInstanceOf(RestaurantNotFound.class);
        verifyNoInteractions(dishes);
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsAMissingDish() {
        customerAccount();
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(dishes.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1)))).isInstanceOf(DishNotFound.class);
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsADishFromAnotherRestaurant() {
        customerAccount();
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(dishes.findById(8L)).thenReturn(Optional.of(dish(8L, 3L, true)));

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(8L, 1))))
                .isInstanceOf(DishRestaurantMismatch.class);
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsAnInactiveDish() {
        customerAccount();
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(dishes.findById(2L)).thenReturn(Optional.of(dish(2L, RESTAURANT_ID, false)));

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(2L, 1)))).isInstanceOf(DishInactive.class);
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsTheFirstInvalidLine() {
        customerAccount();
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(dishes.findById(1L)).thenReturn(Optional.of(dish(1L, RESTAURANT_ID, true)));
        when(dishes.findById(8L)).thenReturn(Optional.of(dish(8L, 3L, true)));

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1), line(8L, 1))))
                .isInstanceOf(DishRestaurantMismatch.class);
        verify(dishes).findById(1L);
        verify(dishes).findById(8L);
        verify(orders, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PENDING", "IN_PREPARATION", "READY"})
    void placeRejectsWhenAnOrderIsInProgress(OrderStatus status) {
        prepareOneDish();
        when(orders.hasActive(CUSTOMER_ID)).thenReturn(OrderStatus.inProgress().contains(status));

        assertThatThrownBy(() -> useCase.place(CUSTOMER, draft(line(1L, 1)))).isInstanceOf(ActiveOrderExists.class);
        verify(orders, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
    void placeStoresAnotherOrderWhenThePreviousOneIsFinished(OrderStatus status) {
        prepareOneDish();
        when(orders.hasActive(CUSTOMER_ID)).thenReturn(OrderStatus.inProgress().contains(status));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 8L));

        Order placed = useCase.place(CUSTOMER, draft(line(1L, 1)));

        assertThat(placed.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(OrderStatus.inProgress()).doesNotContain(status);
        verify(orders).save(any(Order.class));
    }

    private void readyMenu() {
        prepareOneDish();
        when(dishes.findById(4L)).thenReturn(Optional.of(dish(4L, RESTAURANT_ID, true)));
    }

    private void prepareOneDish() {
        customerAccount();
        when(restaurants.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant()));
        when(dishes.findById(1L)).thenReturn(Optional.of(dish(1L, RESTAURANT_ID, true)));
    }

    private void customerAccount() {
        when(owners.findById(CUSTOMER_ID)).thenReturn(Optional.of(new Account(CUSTOMER_ID, Role.CUSTOMER)));
    }

    private static PlaceOrderDraft draft(OrderLineDraft... lines) {
        return new PlaceOrderDraft(RESTAURANT_ID, List.of(lines));
    }

    private static OrderLineDraft line(long dishId, Integer quantity) {
        return new OrderLineDraft(dishId, quantity);
    }

    private static Order withId(Order order, long id) {
        return new Order(
                id,
                order.customerId(),
                order.restaurantId(),
                order.status(),
                order.assignedEmployeeId(),
                order.pinHash(),
                order.lines());
    }

    private static Restaurant restaurant() {
        return new Restaurant(
                RESTAURANT_ID,
                "La Parrilla de Carlos",
                "900100001",
                "Calle 10 # 5-20, Local 101",
                "+576015550101",
                "https://images.example.com/logos/la-parrilla.png",
                2L);
    }

    private static Dish dish(long id, long restaurantId, boolean active) {
        return new Dish(
                id,
                restaurantId,
                "Churrasco",
                45000,
                "Corte de res",
                "https://images.example.com/dishes/churrasco.jpg",
                "Carnes",
                active);
    }

    private enum OrderQuantityCase {
        ZERO(0),
        NEGATIVE(-1),
        ABOVE_MAX(51);

        private final Integer value;

        OrderQuantityCase(int value) {
            this.value = value;
        }
    }
}

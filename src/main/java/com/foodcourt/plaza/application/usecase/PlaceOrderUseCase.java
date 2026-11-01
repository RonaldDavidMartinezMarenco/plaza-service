package com.foodcourt.plaza.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.PlaceOrder;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderLines;
import com.foodcourt.plaza.domain.model.PlaceOrderDraft;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Places an order. A rejection does not save. Checks run in this order:
 * the caller is a {@link Role#CUSTOMER}; the lines are present, unique, and within 1 to 50;
 * the account still exists and is a customer; the restaurant exists; each dish is active
 * and belongs to that restaurant; the customer has no order in progress.
 * The stored order is {@code PENDING}, with no employee and no PIN.
 */
@Service
public final class PlaceOrderUseCase implements PlaceOrder {

    private final OrderRepository orders;
    private final RestaurantRepository restaurants;
    private final CustomerAccountRule accounts;
    private final OrderMenuRule menu;

    public PlaceOrderUseCase(
            OrderRepository orders,
            RestaurantRepository restaurants,
            DishRepository dishes,
            OwnerDirectory owners) {
        this.orders = orders;
        this.restaurants = restaurants;
        this.accounts = new CustomerAccountRule(owners);
        this.menu = new OrderMenuRule(dishes);
    }

    @Override
    public Order place(Caller caller, PlaceOrderDraft draft) {
        requireCustomer(caller);
        List<OrderLine> lines = OrderLines.require(draft.lines());
        accounts.requireCustomer(caller.id());
        requireRestaurant(draft.restaurantId());
        menu.requireOrderable(draft.restaurantId(), lines);
        rejectOrderInProgress(caller.id());
        return orders.save(Order.place(caller.id(), draft.restaurantId(), lines));
    }

    private static void requireCustomer(Caller caller) {
        if (caller.role() != Role.CUSTOMER) {
            throw new AccessDenied();
        }
    }

    private void requireRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new RestaurantNotFound();
        }
        loadRestaurant(restaurantId);
    }

    private void loadRestaurant(long restaurantId) {
        restaurants.findById(restaurantId).orElseThrow(RestaurantNotFound::new);
    }

    private void rejectOrderInProgress(long customerId) {
        if (orders.hasActive(customerId)) {
            throw new ActiveOrderExists();
        }
    }
}

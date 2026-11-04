package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.ListRestaurantOrders;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Lists one page of orders for the restaurant of an {@link Role#EMPLOYEE}.
 * The restaurant id comes from the account. A caller of another role is rejected before that lookup.
 */
@Service
public final class ListRestaurantOrdersUseCase implements ListRestaurantOrders {

    private final OrderRepository orders;
    private final RestaurantRepository restaurants;
    private final EmployeeAccountRule accounts;

    public ListRestaurantOrdersUseCase(
            OrderRepository orders,
            RestaurantRepository restaurants,
            OwnerDirectory owners) {
        this.orders = orders;
        this.restaurants = restaurants;
        this.accounts = new EmployeeAccountRule(owners);
    }

    @Override
    public PageResult<Order> list(Caller caller, OrderListQuery query) {
        requireEmployee(caller);
        long restaurantId = accounts.requireRestaurant(caller.id());
        requireRestaurant(restaurantId);
        return orders.findPage(restaurantId, query);
    }

    private static void requireEmployee(Caller caller) {
        if (caller.role() != Role.EMPLOYEE) {
            throw new AccessDenied();
        }
    }

    private void requireRestaurant(long restaurantId) {
        if (restaurants.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFound();
        }
    }
}

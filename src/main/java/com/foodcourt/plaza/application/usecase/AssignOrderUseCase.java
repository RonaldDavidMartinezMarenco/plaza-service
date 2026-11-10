package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.AssignOrder;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.OrderAlreadyAssigned;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.OrderStatusChange;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OrderTraceRecorder;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Assigns an order to the employee who called and moves it to {@link OrderStatus#IN_PREPARATION}.
 * A rejection does not write. Checks run in this order: the caller is an {@link Role#EMPLOYEE};
 * the account still exists, is an employee, and belongs to a restaurant; that restaurant exists;
 * the order belongs to that restaurant; the order is {@link OrderStatus#PENDING} and unassigned.
 * Two employees can pass the read together. The conditional update keeps a single winner.
 * The loser reads the row again and reports the status now stored.
 */
@Service
public final class AssignOrderUseCase implements AssignOrder {

    private final OrderRepository orders;
    private final RestaurantRepository restaurants;
    private final EmployeeAccountRule accounts;
    private final OrderTraceRule traces;

    public AssignOrderUseCase(
            OrderRepository orders,
            RestaurantRepository restaurants,
            OwnerDirectory owners,
            OrderTraceRecorder traces) {
        this.orders = orders;
        this.restaurants = restaurants;
        this.accounts = new EmployeeAccountRule(owners);
        this.traces = new OrderTraceRule(traces);
    }

    @Override
    public Order assign(Caller caller, long orderId) {
        requireEmployee(caller);
        long restaurantId = accounts.requireRestaurant(caller.id());
        requireRestaurant(restaurantId);
        Order claimed = requireVisible(orderId, restaurantId).assign(caller.id());
        store(orderId, restaurantId, caller.id());
        recordAssignment(claimed, caller.id());
        return claimed;
    }

    private void recordAssignment(Order claimed, long employeeId) {
        traces.record(
                new OrderStatusChange(
                        claimed.id(),
                        claimed.customerId(),
                        claimed.restaurantId(),
                        employeeId,
                        OrderStatus.PENDING,
                        OrderStatus.IN_PREPARATION),
                () -> orders.releaseIfInPreparation(claimed.id(), claimed.restaurantId(), employeeId));
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

    private Order requireVisible(long orderId, long restaurantId) {
        return orders.findByIdAndRestaurantId(orderId, restaurantId).orElseThrow(OrderNotFound::new);
    }

    private void store(long orderId, long restaurantId, long employeeId) {
        if (!orders.assignIfPending(orderId, restaurantId, employeeId)) {
            rejectLostClaim(orderId, restaurantId, employeeId);
        }
    }

    private void rejectLostClaim(long orderId, long restaurantId, long employeeId) {
        failClaim(requireVisible(orderId, restaurantId), employeeId);
    }

    private static void failClaim(Order current, long employeeId) {
        current.assign(employeeId);
        throw new OrderAlreadyAssigned();
    }
}

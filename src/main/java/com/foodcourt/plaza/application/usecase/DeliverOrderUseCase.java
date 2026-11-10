package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.DeliverOrder;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.OrderNotDeliverable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.PinDoesNotMatch;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.OrderStatusChange;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OrderTraceRecorder;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.PinHasher;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Delivers a ready order when the assigned employee presents the PIN.
 * A rejection does not write. Checks run in this order: the caller is an {@link Role#EMPLOYEE};
 * the account still exists, is an employee, and belongs to a restaurant; that restaurant exists;
 * the order belongs to that restaurant; the order is {@link OrderStatus#READY}, assigned to the
 * caller, and has a PIN hash; the PIN matches that hash.
 * Two deliveries can pass the read together. The conditional update keeps a single winner.
 * The loser reads the row again and reports the status now stored.
 */
@Service
public final class DeliverOrderUseCase implements DeliverOrder {

    private final OrderRepository orders;
    private final RestaurantRepository restaurants;
    private final EmployeeAccountRule accounts;
    private final PinHasher hasher;
    private final OrderTraceRule traces;

    public DeliverOrderUseCase(
            OrderRepository orders,
            RestaurantRepository restaurants,
            OwnerDirectory owners,
            PinHasher hasher,
            OrderTraceRecorder traces) {
        this.orders = orders;
        this.restaurants = restaurants;
        this.accounts = new EmployeeAccountRule(owners);
        this.hasher = hasher;
        this.traces = new OrderTraceRule(traces);
    }

    @Override
    public Order deliver(Caller caller, long orderId, String pin) {
        requireEmployee(caller);
        long restaurantId = accounts.requireRestaurant(caller.id());
        requireRestaurant(restaurantId);
        Order current = requireVisible(orderId, restaurantId);
        Order delivered = current.deliver(caller.id());
        requirePin(pin, current.pinHash());
        store(orderId, restaurantId, caller.id(), current.pinHash());
        recordDelivery(current, caller.id());
        return delivered;
    }

    private void recordDelivery(Order current, long employeeId) {
        traces.record(
                new OrderStatusChange(
                        current.id(),
                        current.customerId(),
                        current.restaurantId(),
                        employeeId,
                        OrderStatus.READY,
                        OrderStatus.DELIVERED),
                () -> orders.revertDelivered(current.id(), current.restaurantId(), employeeId, current.pinHash()));
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

    private void requirePin(String pin, String pinHash) {
        if (!hasher.matches(pin, pinHash)) {
            throw new PinDoesNotMatch();
        }
    }

    private void store(long orderId, long restaurantId, long employeeId, String pinHash) {
        if (!orders.deliverIfReady(orderId, restaurantId, employeeId, pinHash)) {
            lost(orderId, restaurantId, employeeId);
        }
    }

    private void lost(long orderId, long restaurantId, long employeeId) {
        requireVisible(orderId, restaurantId).deliver(employeeId);
        throw new OrderNotDeliverable(OrderStatus.READY);
    }
}

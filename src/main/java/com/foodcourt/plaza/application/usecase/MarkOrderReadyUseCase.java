package com.foodcourt.plaza.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.MarkOrderReady;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.NotificationRejected;
import com.foodcourt.plaza.domain.exception.NotificationUnavailable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.exception.OrderNotReady;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.OrderStatusChange;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.model.SecurityPin;
import com.foodcourt.plaza.domain.spi.CustomerPhoneDirectory;
import com.foodcourt.plaza.domain.spi.OrderReadyNotifier;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OrderTraceRecorder;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.PinGenerator;
import com.foodcourt.plaza.domain.spi.PinHasher;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Marks an order ready and sends the PIN. A rejection does not leave the order {@code READY}.
 * Checks run in this order: the caller is an {@link Role#EMPLOYEE}; the account still exists and
 * belongs to a restaurant; that restaurant exists; the order belongs to that restaurant; the order
 * is {@link OrderStatus#IN_PREPARATION} and assigned to the caller; the customer has a phone.
 * The conditional update keeps a single winner. The SMS is sent only after that update.
 */
@Service
public final class MarkOrderReadyUseCase implements MarkOrderReady {

    private static final Logger log = LoggerFactory.getLogger(MarkOrderReadyUseCase.class);

    private final OrderRepository orders;
    private final RestaurantRepository restaurants;
    private final EmployeeAccountRule accounts;
    private final CustomerPhoneDirectory customers;
    private final PinGenerator pins;
    private final PinHasher hasher;
    private final OrderReadyNotifier notifier;
    private final OrderTraceRule traces;

    public MarkOrderReadyUseCase(
            OrderRepository orders,
            RestaurantRepository restaurants,
            OwnerDirectory owners,
            CustomerPhoneDirectory customers,
            PinGenerator pins,
            PinHasher hasher,
            OrderReadyNotifier notifier,
            OrderTraceRecorder traces) {
        this.orders = orders;
        this.restaurants = restaurants;
        this.accounts = new EmployeeAccountRule(owners);
        this.customers = customers;
        this.pins = pins;
        this.hasher = hasher;
        this.notifier = notifier;
        this.traces = new OrderTraceRule(traces);
    }

    @Override
    public Order markReady(Caller caller, long orderId) {
        requireEmployee(caller);
        long restaurantId = accounts.requireRestaurant(caller.id());
        Restaurant restaurant = requireRestaurant(restaurantId);
        Order current = requireVisible(orderId, restaurantId);
        Order ready = current.markReady(caller.id());
        String phone = customers.findPhone(current.customerId());
        String pin = new SecurityPin(pins.generate()).value();
        String hash = hasher.hash(pin);
        store(orderId, restaurantId, caller.id(), hash);
        deliver(orderId, restaurantId, restaurant.name(), caller.id(), phone, pin, hash);
        recordReady(current, caller.id(), hash);
        return ready.withPin(hash);
    }

    private void recordReady(Order current, long employeeId, String hash) {
        traces.record(
                new OrderStatusChange(
                        current.id(),
                        current.customerId(),
                        current.restaurantId(),
                        employeeId,
                        OrderStatus.IN_PREPARATION,
                        OrderStatus.READY),
                () -> orders.revertReady(current.id(), current.restaurantId(), employeeId, hash));
    }

    private static void requireEmployee(Caller caller) {
        if (caller.role() != Role.EMPLOYEE) {
            throw new AccessDenied();
        }
    }

    private Restaurant requireRestaurant(long restaurantId) {
        return restaurants.findById(restaurantId).orElseThrow(RestaurantNotFound::new);
    }

    private Order requireVisible(long orderId, long restaurantId) {
        return orders.findByIdAndRestaurantId(orderId, restaurantId).orElseThrow(OrderNotFound::new);
    }

    private void store(long orderId, long restaurantId, long employeeId, String hash) {
        if (!orders.markReadyIfInPreparation(orderId, restaurantId, employeeId, hash)) {
            lost(orderId, restaurantId, employeeId);
        }
    }

    private void lost(long orderId, long restaurantId, long employeeId) {
        requireVisible(orderId, restaurantId).markReady(employeeId);
        throw new OrderNotReady(OrderStatus.IN_PREPARATION);
    }

    private void deliver(
            long orderId,
            long restaurantId,
            String restaurantName,
            long employeeId,
            String phone,
            String pin,
            String hash) {
        try {
            notifier.notifyReady(orderId, restaurantName, phone, pin);
        } catch (NotificationRejected | NotificationUnavailable failure) {
            undo(orderId, restaurantId, employeeId, hash);
            throw failure;
        }
    }

    private void undo(long orderId, long restaurantId, long employeeId, String hash) {
        if (!orders.revertReady(orderId, restaurantId, employeeId, hash)) {
            log.error("Order {} stayed READY after the SMS was rejected", orderId);
        }
    }
}

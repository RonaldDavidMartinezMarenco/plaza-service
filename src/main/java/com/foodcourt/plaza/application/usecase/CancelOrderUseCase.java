package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CancelOrder;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.OrderNotCancellable;
import com.foodcourt.plaza.domain.exception.OrderNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.OrderStatusChange;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.domain.spi.OrderTraceRecorder;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;

/**
 * Cancels the caller's order while it is {@link OrderStatus#PENDING}.
 * A rejection does not write. Checks run in this order: the caller is a {@link Role#CUSTOMER};
 * the account still exists and is a customer; the order belongs to that customer; the order is pending.
 * A cancellation and an assignment can pass the read together. The conditional update keeps a single winner.
 * The loser reads the row again and reports the status now stored.
 */
@Service
public final class CancelOrderUseCase implements CancelOrder {

    private final OrderRepository orders;
    private final CustomerAccountRule accounts;
    private final OrderTraceRule traces;

    public CancelOrderUseCase(OrderRepository orders, OwnerDirectory owners, OrderTraceRecorder traces) {
        this.orders = orders;
        this.accounts = new CustomerAccountRule(owners);
        this.traces = new OrderTraceRule(traces);
    }

    @Override
    public Order cancel(Caller caller, long orderId) {
        requireCustomer(caller);
        accounts.requireCustomer(caller.id());
        Order cancelled = requireOwn(orderId, caller.id()).cancel();
        store(orderId, caller.id());
        recordCancellation(cancelled);
        return cancelled;
    }

    private void recordCancellation(Order cancelled) {
        traces.record(
                new OrderStatusChange(
                        cancelled.id(),
                        cancelled.customerId(),
                        cancelled.restaurantId(),
                        null,
                        OrderStatus.PENDING,
                        OrderStatus.CANCELLED),
                () -> orders.revertCancellation(cancelled.id(), cancelled.customerId()));
    }

    private static void requireCustomer(Caller caller) {
        if (caller.role() != Role.CUSTOMER) {
            throw new AccessDenied();
        }
    }

    private Order requireOwn(long orderId, long customerId) {
        return orders.findByIdAndCustomerId(orderId, customerId).orElseThrow(OrderNotFound::new);
    }

    private void store(long orderId, long customerId) {
        if (!orders.cancelIfPending(orderId, customerId)) {
            lost(orderId, customerId);
        }
    }

    private void lost(long orderId, long customerId) {
        fail(requireOwn(orderId, customerId));
    }

    private static void fail(Order current) {
        current.cancel();
        throw OrderNotCancellable.unchanged();
    }
}

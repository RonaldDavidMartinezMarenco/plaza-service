package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.spi.OrderRepository;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.CustomerOrderEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.mapper.OrderJpaMapper;
import com.foodcourt.plaza.infrastructure.output.jpa.repository.CustomerOrderJpaRepository;

/**
 * JPA adapter for {@link OrderRepository}.
 * The order and its lines commit together. A concurrent insert can pass {@link #hasActive}
 * and still hit {@code customer_order_one_active_idx}. PostgreSQL reports that as SQLState {@code 23505}.
 * The class is not final: {@code @Transactional} needs a CGLIB subclass.
 */
@Service
public class OrderRepositoryAdapter implements OrderRepository {

    /** PostgreSQL {@code unique_violation}. */
    static final String UNIQUE_VIOLATION = "23505";

    private static final String ACTIVE_ORDER_INDEX = "customer_order_one_active_idx";

    private final CustomerOrderJpaRepository orders;
    private final OrderJpaMapper mapper = new OrderJpaMapper();

    public OrderRepositoryAdapter(CustomerOrderJpaRepository orders) {
        this.orders = orders;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActive(long customerId) {
        return orders.existsByCustomerIdAndStatusIn(customerId, inProgress());
    }

    @Override
    @Transactional
    public Order save(Order order) {
        try {
            CustomerOrderEntity stored = orders.saveAndFlush(mapper.toEntity(order));
            return mapper.toDomain(stored);
        } catch (DataIntegrityViolationException failure) {
            throw translated(failure);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> findPage(long restaurantId, OrderListQuery query) {
        Page<CustomerOrderEntity> stored = orders.findByRestaurantIdAndStatus(
                restaurantId,
                query.status(),
                PageRequest.of(query.page().page(), query.page().size(), Sort.by("id")));
        if (stored.isEmpty()) {
            return mapper.toPage(stored, List.of());
        }
        return mapper.toPage(stored, orders.findByIdIn(ids(stored)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByIdAndRestaurantId(long orderId, long restaurantId) {
        return orders.findByIdAndRestaurantId(orderId, restaurantId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public boolean assignIfPending(long orderId, long restaurantId, long employeeId) {
        int updated = orders.assignIfPending(
                orderId,
                restaurantId,
                employeeId,
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION);
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean markReadyIfInPreparation(long orderId, long restaurantId, long employeeId, String pinHash) {
        int updated = orders.markReadyIfInPreparation(
                orderId,
                restaurantId,
                employeeId,
                pinHash,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY);
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean revertReady(long orderId, long restaurantId, long employeeId, String pinHash) {
        int updated = orders.revertReady(
                orderId,
                restaurantId,
                employeeId,
                pinHash,
                OrderStatus.READY,
                OrderStatus.IN_PREPARATION);
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean deliverIfReady(long orderId, long restaurantId, long employeeId, String pinHash) {
        int updated = orders.deliverIfReady(
                orderId,
                restaurantId,
                employeeId,
                pinHash,
                OrderStatus.READY,
                OrderStatus.DELIVERED);
        return updated == 1;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByIdAndCustomerId(long orderId, long customerId) {
        return orders.findByIdAndCustomerId(orderId, customerId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public boolean cancelIfPending(long orderId, long customerId) {
        int updated = orders.cancelIfPending(
                orderId,
                customerId,
                OrderStatus.PENDING,
                OrderStatus.CANCELLED);
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean deleteIfPending(long orderId, long customerId) {
        Optional<CustomerOrderEntity> stored = orders.findByIdAndCustomerId(orderId, customerId);
        if (missingPending(stored)) {
            return false;
        }
        orders.delete(stored.get());
        return true;
    }

    @Override
    @Transactional
    public boolean releaseIfInPreparation(long orderId, long restaurantId, long employeeId) {
        int updated = orders.releaseIfInPreparation(
                orderId,
                restaurantId,
                employeeId,
                OrderStatus.IN_PREPARATION,
                OrderStatus.PENDING);
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean revertDelivered(long orderId, long restaurantId, long employeeId, String pinHash) {
        int updated = orders.revertDelivered(
                orderId,
                restaurantId,
                employeeId,
                pinHash,
                OrderStatus.DELIVERED,
                OrderStatus.READY);
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean revertCancellation(long orderId, long customerId) {
        int updated = orders.revertCancellation(
                orderId,
                customerId,
                OrderStatus.CANCELLED,
                OrderStatus.PENDING);
        return updated == 1;
    }

    private static boolean missingPending(Optional<CustomerOrderEntity> stored) {
        return stored.isEmpty() || stored.get().getStatus() != OrderStatus.PENDING;
    }

    private static List<Long> ids(Page<CustomerOrderEntity> stored) {
        return stored.getContent().stream().map(CustomerOrderEntity::getId).toList();
    }

    private static Collection<OrderStatus> inProgress() {
        return OrderStatus.inProgress();
    }

    private RuntimeException translated(DataIntegrityViolationException failure) {
        if (isActiveOrderConflict(failure)) {
            return new ActiveOrderExists();
        }
        return failure;
    }

    private static boolean isActiveOrderConflict(DataIntegrityViolationException failure) {
        return isUniqueViolation(failure) && mentionsActiveOrder(causeMessage(failure));
    }

    private static boolean isUniqueViolation(DataIntegrityViolationException failure) {
        Throwable cause = failure.getMostSpecificCause();
        if (cause instanceof SQLException sqlException) {
            return UNIQUE_VIOLATION.equals(sqlException.getSQLState());
        }
        return false;
    }

    private static String causeMessage(DataIntegrityViolationException failure) {
        return failure.getMostSpecificCause().getMessage();
    }

    private static boolean mentionsActiveOrder(String message) {
        return message != null && message.contains(ACTIVE_ORDER_INDEX);
    }
}

package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collection;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderListQuery;
import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.CustomerOrderEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.OrderLineEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.repository.CustomerOrderJpaRepository;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    private static final long CUSTOMER_ID = 9L;

    @Mock
    private CustomerOrderJpaRepository orders;

    private OrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderRepositoryAdapter(orders);
    }

    @Test
    void hasActiveAsksOnlyForOrdersInProgress() {
        when(orders.existsByCustomerIdAndStatusIn(eq(CUSTOMER_ID), any())).thenReturn(true);

        assertThat(adapter.hasActive(CUSTOMER_ID)).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<OrderStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(orders).existsByCustomerIdAndStatusIn(eq(CUSTOMER_ID), statuses.capture());
        assertThat(statuses.getValue()).containsExactly(
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY);
    }

    @Test
    void saveStoresTheOrderAndItsLines() {
        when(orders.saveAndFlush(any(CustomerOrderEntity.class))).thenAnswer(invocation -> stored(invocation.getArgument(0)));

        Order saved = adapter.save(Order.place(CUSTOMER_ID, 1L, List.of(new OrderLine(1L, 1), new OrderLine(4L, 2))));

        ArgumentCaptor<CustomerOrderEntity> sent = ArgumentCaptor.forClass(CustomerOrderEntity.class);
        verify(orders).saveAndFlush(sent.capture());
        CustomerOrderEntity row = sent.getValue();
        assertThat(row.getId()).isNull();
        assertThat(row.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(row.getRestaurantId()).isEqualTo(1L);
        assertThat(row.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(row.getAssignedEmployeeId()).isNull();
        assertThat(row.getPinHash()).isNull();
        assertThat(row.getLines()).extracting(OrderLineEntity::getDishId).containsExactly(1L, 4L);
        assertThat(row.getLines()).extracting(OrderLineEntity::getQuantity).containsExactly(1, 2);
        assertThat(saved.id()).isEqualTo(7L);
        assertThat(saved.lines()).containsExactly(new OrderLine(1L, 1), new OrderLine(4L, 2));
        assertThat(saved.pinHash()).isNull();
    }

    @Test
    void saveRejectsASecondOrderInProgress() {
        when(orders.saveAndFlush(any(CustomerOrderEntity.class)))
                .thenThrow(unique("duplicate key value violates unique constraint \"customer_order_one_active_idx\""));

        assertThatThrownBy(() -> adapter.save(Order.place(CUSTOMER_ID, 1L, List.of(new OrderLine(1L, 1)))))
                .isInstanceOf(ActiveOrderExists.class);
    }

    @Test
    void saveRethrowsAUniqueViolationItCannotClassify() {
        DataIntegrityViolationException failure = unique("duplicate key value violates unique constraint \"one_line_per_dish\"");
        when(orders.saveAndFlush(any(CustomerOrderEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(Order.place(CUSTOMER_ID, 1L, List.of(new OrderLine(1L, 1)))))
                .isSameAs(failure);
    }

    @Test
    void saveRethrowsAnIntegrityFailureThatIsNotUnique() {
        DataIntegrityViolationException failure = new DataIntegrityViolationException(
                "null",
                new SQLException("null value", "23502"));
        when(orders.saveAndFlush(any(CustomerOrderEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(Order.place(CUSTOMER_ID, 1L, List.of(new OrderLine(1L, 1)))))
                .isSameAs(failure);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void findPageFiltersByRestaurantStatusAndId(OrderStatus status) {
        CustomerOrderEntity row = order(4L, 1L);
        when(orders.findByRestaurantIdAndStatus(eq(1L), eq(status), any())).thenReturn(new PageImpl<>(List.of(row)));
        when(orders.findByIdIn(List.of(4L))).thenReturn(List.of(row));

        PageResult<Order> result = adapter.findPage(1L, new OrderListQuery(new PageQuery(0, 10), status));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(orders).findByRestaurantIdAndStatus(eq(1L), eq(status), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        assertThat(result.items()).extracting(Order::id).containsExactly(4L);
        assertThat(result.items().get(0).lines()).containsExactly(new OrderLine(1L, 1));
    }

    @Test
    void findPageSkipsLinesWhenThePageIsEmpty() {
        Page<CustomerOrderEntity> empty = new PageImpl<>(List.of(), PageRequest.of(3, 10), 0);
        when(orders.findByRestaurantIdAndStatus(eq(1L), eq(OrderStatus.CANCELLED), any())).thenReturn(empty);

        PageResult<Order> result = adapter.findPage(
                1L,
                new OrderListQuery(new PageQuery(3, 10), OrderStatus.CANCELLED));

        verify(orders, never()).findByIdIn(any());
        assertThat(result.items()).isEmpty();
        assertThat(result.page()).isEqualTo(3);
        assertThat(result.totalItems()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void findPageKeepsThePageOrderWhenLinesComeBackReversed() {
        CustomerOrderEntity first = order(1L, 1L);
        CustomerOrderEntity second = order(2L, 4L);
        when(orders.findByRestaurantIdAndStatus(eq(1L), eq(OrderStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(first, second)));
        when(orders.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(second, first));

        PageResult<Order> result = adapter.findPage(1L, new OrderListQuery(new PageQuery(0, 10), OrderStatus.PENDING));

        assertThat(result.items()).extracting(Order::id).containsExactly(1L, 2L);
        assertThat(result.items().get(0).lines()).containsExactly(new OrderLine(1L, 1));
        assertThat(result.items().get(1).lines()).containsExactly(new OrderLine(4L, 1));
    }

    @Test
    void findByIdAndRestaurantIdLoadsTheLines() {
        CustomerOrderEntity row = order(1L, 1L);
        when(orders.findByIdAndRestaurantId(1L, 1L)).thenReturn(Optional.of(row));

        assertThat(adapter.findByIdAndRestaurantId(1L, 1L)).contains(new Order(
                1L,
                CUSTOMER_ID,
                1L,
                OrderStatus.PENDING,
                null,
                "hash",
                List.of(new OrderLine(1L, 1))));
    }

    @Test
    void findByIdAndRestaurantIdIsEmptyWhenTheRowIsMissing() {
        when(orders.findByIdAndRestaurantId(9L, 2L)).thenReturn(Optional.empty());

        assertThat(adapter.findByIdAndRestaurantId(9L, 2L)).isEmpty();
    }

    @Test
    void assignIfPendingIsTrueWhenOneRowChanges() {
        when(orders.assignIfPending(1L, 1L, 5L, OrderStatus.PENDING, OrderStatus.IN_PREPARATION)).thenReturn(1);

        assertThat(adapter.assignIfPending(1L, 1L, 5L)).isTrue();
        verify(orders).assignIfPending(1L, 1L, 5L, OrderStatus.PENDING, OrderStatus.IN_PREPARATION);
    }

    @Test
    void markReadyIfInPreparationIsTrueWhenOneRowChanges() {
        when(orders.markReadyIfInPreparation(1L, 1L, 5L, "hash", OrderStatus.IN_PREPARATION, OrderStatus.READY))
                .thenReturn(1);

        assertThat(adapter.markReadyIfInPreparation(1L, 1L, 5L, "hash")).isTrue();
    }

    @Test
    void revertReadyIsFalseWhenTheHashNoLongerMatches() {
        when(orders.revertReady(1L, 1L, 5L, "hash", OrderStatus.READY, OrderStatus.IN_PREPARATION)).thenReturn(0);

        assertThat(adapter.revertReady(1L, 1L, 5L, "hash")).isFalse();
    }

    @Test
    void deliverIfReadyIsTrueOnlyWhenOneReadyRowChanges() {
        when(orders.deliverIfReady(1L, 1L, 5L, "hash", OrderStatus.READY, OrderStatus.DELIVERED)).thenReturn(1);

        assertThat(adapter.deliverIfReady(1L, 1L, 5L, "hash")).isTrue();
        verify(orders).deliverIfReady(1L, 1L, 5L, "hash", OrderStatus.READY, OrderStatus.DELIVERED);
    }

    @Test
    void deliverIfReadyIsFalseWhenNoRowChanges() {
        when(orders.deliverIfReady(1L, 1L, 5L, "hash", OrderStatus.READY, OrderStatus.DELIVERED)).thenReturn(0);

        assertThat(adapter.deliverIfReady(1L, 1L, 5L, "hash")).isFalse();
    }

    @Test
    void assignIfPendingIsFalseWhenNoRowChanges() {
        when(orders.assignIfPending(1L, 1L, 6L, OrderStatus.PENDING, OrderStatus.IN_PREPARATION)).thenReturn(0);

        assertThat(adapter.assignIfPending(1L, 1L, 6L)).isFalse();
    }

    @Test
    void findByIdAndCustomerIdLoadsTheLines() {
        when(orders.findByIdAndCustomerId(2L, 12L)).thenReturn(Optional.of(order(4L, 1L)));

        Optional<Order> found = adapter.findByIdAndCustomerId(2L, 12L);

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().lines()).containsExactly(new OrderLine(1L, 1));
    }

    @Test
    void findByIdAndCustomerIdIsEmptyWhenTheRowIsMissing() {
        when(orders.findByIdAndCustomerId(2L, 9L)).thenReturn(Optional.empty());

        assertThat(adapter.findByIdAndCustomerId(2L, 9L)).isEmpty();
    }

    @Test
    void cancelIfPendingIsTrueWhenOneRowChanges() {
        when(orders.cancelIfPending(2L, 12L, OrderStatus.PENDING, OrderStatus.CANCELLED)).thenReturn(1);

        assertThat(adapter.cancelIfPending(2L, 12L)).isTrue();
        verify(orders).cancelIfPending(2L, 12L, OrderStatus.PENDING, OrderStatus.CANCELLED);
    }

    @Test
    void cancelIfPendingIsFalseWhenNoRowChanges() {
        when(orders.cancelIfPending(2L, 12L, OrderStatus.PENDING, OrderStatus.CANCELLED)).thenReturn(0);

        assertThat(adapter.cancelIfPending(2L, 12L)).isFalse();
    }

    private static CustomerOrderEntity order(long id, long dishId) {
        CustomerOrderEntity entity = new CustomerOrderEntity(id, CUSTOMER_ID, 1L, OrderStatus.PENDING, null, "hash");
        entity.addLine(new OrderLineEntity(id, dishId, 1));
        return entity;
    }

    private static CustomerOrderEntity stored(CustomerOrderEntity row) {
        CustomerOrderEntity stored = new CustomerOrderEntity(
                7L,
                row.getCustomerId(),
                row.getRestaurantId(),
                row.getStatus(),
                row.getAssignedEmployeeId(),
                row.getPinHash());
        for (OrderLineEntity line : row.getLines()) {
            stored.addLine(new OrderLineEntity(line.getId(), line.getDishId(), line.getQuantity()));
        }
        return stored;
    }

    private static DataIntegrityViolationException unique(String message) {
        return new DataIntegrityViolationException(
                "conflict",
                new SQLException(message, OrderRepositoryAdapter.UNIQUE_VIOLATION));
    }
}

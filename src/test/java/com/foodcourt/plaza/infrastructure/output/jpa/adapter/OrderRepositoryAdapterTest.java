package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.foodcourt.plaza.domain.exception.ActiveOrderExists;
import com.foodcourt.plaza.domain.model.Order;
import com.foodcourt.plaza.domain.model.OrderLine;
import com.foodcourt.plaza.domain.model.OrderStatus;
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

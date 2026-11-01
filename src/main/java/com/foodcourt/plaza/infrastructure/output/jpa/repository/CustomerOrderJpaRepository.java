package com.foodcourt.plaza.infrastructure.output.jpa.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.CustomerOrderEntity;

/** Spring Data access to {@code customer_order}. Used only by the JPA adapter. */
public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrderEntity, Long> {

    boolean existsByCustomerIdAndStatusIn(long customerId, Collection<OrderStatus> statuses);
}

package com.foodcourt.plaza.infrastructure.output.jpa.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.CustomerOrderEntity;

/** Spring Data access to {@code customer_order}. Used only by the JPA adapter. */
public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrderEntity, Long> {

    boolean existsByCustomerIdAndStatusIn(long customerId, Collection<OrderStatus> statuses);

    Page<CustomerOrderEntity> findByRestaurantIdAndStatus(long restaurantId, OrderStatus status, Pageable pageable);

    /** Loads lines for the ids of one page. The page query itself stays free of the collection. */
    @EntityGraph(attributePaths = "lines")
    List<CustomerOrderEntity> findByIdIn(Collection<Long> ids);
}

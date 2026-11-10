package com.foodcourt.plaza.infrastructure.output.jpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodcourt.plaza.domain.model.OrderStatus;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.CustomerOrderEntity;

/** Spring Data access to {@code customer_order}. Used only by the JPA adapter. */
public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrderEntity, Long> {

    boolean existsByCustomerIdAndStatusIn(long customerId, Collection<OrderStatus> statuses);

    Page<CustomerOrderEntity> findByRestaurantIdAndStatus(long restaurantId, OrderStatus status, Pageable pageable);

    /** Loads lines for the ids of one page. The page query itself stays free of the collection. */
    @EntityGraph(attributePaths = "lines")
    List<CustomerOrderEntity> findByIdIn(Collection<Long> ids);

    /** One order of one restaurant, with its lines. Another restaurant looks like a missing id. */
    @EntityGraph(attributePaths = "lines")
    Optional<CustomerOrderEntity> findByIdAndRestaurantId(long id, long restaurantId);

    /**
     * Claims a row that is still pending and unassigned.
     * Returns the number of rows changed. Zero means another request won.
     */
    @Modifying
    @Query("""
            update CustomerOrderEntity o
               set o.status = :nextStatus,
                   o.assignedEmployeeId = :employeeId
             where o.id = :id
               and o.restaurantId = :restaurantId
               and o.status = :pending
               and o.assignedEmployeeId is null
            """)
            int assignIfPending(
            @Param("id") long id,
            @Param("restaurantId") long restaurantId,
            @Param("employeeId") long employeeId,
            @Param("pending") OrderStatus pending,
            @Param("nextStatus") OrderStatus nextStatus);

    /**
     * Moves a row to ready only while it is still in preparation for this employee and has no PIN.
     * Returns the number of rows changed. Zero means another request won.
     */
    @Modifying
    @Query("""
            update CustomerOrderEntity o
               set o.status = :ready,
                   o.pinHash = :pinHash
             where o.id = :id
               and o.restaurantId = :restaurantId
               and o.status = :preparing
               and o.assignedEmployeeId = :employeeId
               and o.pinHash is null
            """)
    int markReadyIfInPreparation(
            @Param("id") long id,
            @Param("restaurantId") long restaurantId,
            @Param("employeeId") long employeeId,
            @Param("pinHash") String pinHash,
            @Param("preparing") OrderStatus preparing,
            @Param("ready") OrderStatus ready);

    /**
     * Returns a ready row to preparation only when the PIN hash is still the one this request stored.
     */
    @Modifying
    @Query("""
            update CustomerOrderEntity o
               set o.status = :preparing,
                   o.pinHash = null
             where o.id = :id
               and o.restaurantId = :restaurantId
               and o.status = :ready
               and o.assignedEmployeeId = :employeeId
               and o.pinHash = :pinHash
            """)
    int revertReady(
            @Param("id") long id,
            @Param("restaurantId") long restaurantId,
            @Param("employeeId") long employeeId,
            @Param("pinHash") String pinHash,
            @Param("ready") OrderStatus ready,
            @Param("preparing") OrderStatus preparing);

    /**
     * Moves a row to delivered only while it is still ready for this employee and this PIN hash.
     * Returns the number of rows changed. Zero means another request won.
     */
    @Modifying
    @Query("""
            update CustomerOrderEntity o
               set o.status = :delivered
             where o.id = :id
               and o.restaurantId = :restaurantId
               and o.status = :ready
               and o.assignedEmployeeId = :employeeId
               and o.pinHash = :pinHash
            """)
    int deliverIfReady(
            @Param("id") long id,
            @Param("restaurantId") long restaurantId,
            @Param("employeeId") long employeeId,
            @Param("pinHash") String pinHash,
            @Param("ready") OrderStatus ready,
            @Param("delivered") OrderStatus delivered);
}

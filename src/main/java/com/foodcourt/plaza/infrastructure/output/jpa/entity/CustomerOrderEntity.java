package com.foodcourt.plaza.infrastructure.output.jpa.entity;

import java.util.ArrayList;
import java.util.List;

import com.foodcourt.plaza.domain.model.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Row of {@code customer_order}. Lines are saved with the order.
 * {@code pin_hash} stays null until a later story.
 */
@Entity
@Table(name = "customer_order")
public class CustomerOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "assigned_employee_id")
    private Long assignedEmployeeId;

    @Column(name = "pin_hash", length = 72)
    private String pinHash;

    @OrderBy("id ASC")
    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<OrderLineEntity> lines = new ArrayList<>();

    protected CustomerOrderEntity() {
    }

    public CustomerOrderEntity(
            Long id,
            Long customerId,
            Long restaurantId,
            OrderStatus status,
            Long assignedEmployeeId,
            String pinHash) {
        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.assignedEmployeeId = assignedEmployeeId;
        this.pinHash = pinHash;
    }

    public void addLine(OrderLineEntity line) {
        line.attachTo(this);
        lines.add(line);
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public String getPinHash() {
        return pinHash;
    }

    public List<OrderLineEntity> getLines() {
        return lines;
    }
}

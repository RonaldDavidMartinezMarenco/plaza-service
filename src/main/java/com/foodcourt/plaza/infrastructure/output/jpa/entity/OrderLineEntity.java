package com.foodcourt.plaza.infrastructure.output.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Row of {@code order_line}. One dish appears once per order. */
@Entity
@Table(name = "order_line")
public class OrderLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrderEntity order;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    protected OrderLineEntity() {
    }

    public OrderLineEntity(Long id, Long dishId, Integer quantity) {
        this.id = id;
        this.dishId = dishId;
        this.quantity = quantity;
    }

    void attachTo(CustomerOrderEntity order) {
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public Long getDishId() {
        return dishId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}

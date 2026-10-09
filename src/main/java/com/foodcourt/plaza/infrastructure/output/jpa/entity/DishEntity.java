package com.foodcourt.plaza.infrastructure.output.jpa.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Row of {@code dish}. Text columns use {@link SqlTypes#LONGVARCHAR} so they
 * match PostgreSQL {@code text}. {@code restaurant_id} references {@code restaurant}.
 */
@Entity
@Table(name = "dish")
public class DishEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "description", nullable = false)
    private String description;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected DishEntity() {
    }

    public DishEntity(
            Long id,
            Long restaurantId,
            String name,
            Integer price,
            String description,
            String imageUrl,
            String category,
            boolean active) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }
}

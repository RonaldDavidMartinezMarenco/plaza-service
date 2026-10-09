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
 * Row of {@code restaurant}. Text columns use {@link SqlTypes#LONGVARCHAR} so they
 * match PostgreSQL {@code text}. {@code owner_id} is a logical reference to user-service.
 */
@Entity
@Table(name = "restaurant")
public class RestaurantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "name", nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "nit", nullable = false, unique = true)
    private String nit;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone", nullable = false, length = 13)
    private String phone;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "logo_url", nullable = false)
    private String logoUrl;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    protected RestaurantEntity() {
    }

    public RestaurantEntity(
            Long id,
            String name,
            String nit,
            String address,
            String phone,
            String logoUrl,
            Long ownerId) {
        this.id = id;
        this.name = name;
        this.nit = nit;
        this.address = address;
        this.phone = phone;
        this.logoUrl = logoUrl;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNit() {
        return nit;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}

package com.foodcourt.plaza.domain.model;

/**
 * Input of restaurant creation. {@code id} is assigned by PostgreSQL on insert.
 * {@code name} may still have surrounding spaces; the use case trims it.
 */
public record NewRestaurant(
        String name,
        String nit,
        String address,
        String phone,
        String logoUrl,
        Long ownerId) {
}

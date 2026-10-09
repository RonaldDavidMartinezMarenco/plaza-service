package com.foodcourt.plaza.domain.model;

/**
 * Persisted restaurant. {@code id} is null until PostgreSQL assigns it.
 * {@code ownerId} points at an account in user-service; this database has no foreign key.
 */
public record Restaurant(
        Long id,
        String name,
        String nit,
        String address,
        String phone,
        String logoUrl,
        Long ownerId) {
}

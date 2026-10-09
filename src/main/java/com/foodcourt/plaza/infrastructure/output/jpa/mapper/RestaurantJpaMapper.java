package com.foodcourt.plaza.infrastructure.output.jpa.mapper;

import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.RestaurantEntity;

/** Copies fields between {@link RestaurantEntity} and {@link Restaurant}. Applies no rule. */
public final class RestaurantJpaMapper {

    public RestaurantEntity toEntity(Restaurant restaurant) {
        return new RestaurantEntity(
                restaurant.id(),
                restaurant.name(),
                restaurant.nit(),
                restaurant.address(),
                restaurant.phone(),
                restaurant.logoUrl(),
                restaurant.ownerId());
    }

    public Restaurant toDomain(RestaurantEntity entity) {
        return new Restaurant(
                entity.getId(),
                entity.getName(),
                entity.getNit(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getLogoUrl(),
                entity.getOwnerId());
    }
}

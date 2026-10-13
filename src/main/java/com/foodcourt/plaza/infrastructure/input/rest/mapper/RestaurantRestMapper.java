package com.foodcourt.plaza.infrastructure.input.rest.mapper;

import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.infrastructure.input.rest.dto.CreateRestaurantRequest;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantOwnershipResponse;
import com.foodcourt.plaza.infrastructure.input.rest.dto.RestaurantResponse;

/** Copies fields between the HTTP contract and the domain. Applies no rule. */
public final class RestaurantRestMapper {

    public NewRestaurant toNewRestaurant(CreateRestaurantRequest request) {
        return new NewRestaurant(
                request.name(),
                request.nit(),
                request.address(),
                request.phone(),
                request.logoUrl(),
                request.ownerId());
    }

    public RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.id(),
                restaurant.name(),
                restaurant.nit(),
                restaurant.address(),
                restaurant.phone(),
                restaurant.logoUrl(),
                restaurant.ownerId());
    }

    public RestaurantOwnershipResponse toOwnershipResponse(Restaurant restaurant) {
        return new RestaurantOwnershipResponse(restaurant.id(), restaurant.ownerId());
    }
}

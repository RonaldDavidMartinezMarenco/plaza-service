package com.foodcourt.plaza.application.usecase;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * The caller is an {@link Role#OWNER} of the restaurant, and that restaurant's
 * account is still an owner. {@link #requireOwnedBy} checks the role again so a
 * caller that is not an owner is rejected even when {@link #requireOwnerRole}
 * was not called first.
 */
final class RestaurantOwnershipRule {

    private final RestaurantRepository restaurants;
    private final OwnerAccountRule ownerAccounts;

    RestaurantOwnershipRule(RestaurantRepository restaurants, OwnerAccountRule ownerAccounts) {
        this.restaurants = restaurants;
        this.ownerAccounts = ownerAccounts;
    }

    void requireOwnerRole(Caller caller) {
        if (caller.role() != Role.OWNER) {
            throw new AccessDenied();
        }
    }

    Restaurant requireOwnedBy(Caller caller, long restaurantId) {
        requireOwnerRole(caller);
        Restaurant restaurant = requireRestaurant(restaurantId);
        requireOwns(caller, restaurant);
        ownerAccounts.requireOwner(restaurant.ownerId());
        return restaurant;
    }

    private Restaurant requireRestaurant(long restaurantId) {
        return restaurants.findById(restaurantId).orElseThrow(RestaurantNotFound::new);
    }

    private static void requireOwns(Caller caller, Restaurant restaurant) {
        if (caller.id() != restaurant.ownerId()) {
            throw new AccessDenied();
        }
    }
}

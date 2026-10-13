package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.FindRestaurant;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Reads a restaurant so user-service can check who owns it.
 * The caller is an {@link Role#OWNER} or an {@link Role#ADMINISTRATOR}; then the restaurant must exist.
 * Whether the caller owns it is decided by the service that asked.
 */
@Service
public final class FindRestaurantUseCase implements FindRestaurant {

    private final RestaurantRepository restaurants;

    public FindRestaurantUseCase(RestaurantRepository restaurants) {
        this.restaurants = restaurants;
    }

    @Override
    public Restaurant find(Caller caller, long id) {
        requireOwnerOrAdministrator(caller);
        return restaurants.findById(id).orElseThrow(RestaurantNotFound::new);
    }

    private static void requireOwnerOrAdministrator(Caller caller) {
        if (caller.role() != Role.OWNER && caller.role() != Role.ADMINISTRATOR) {
            throw new AccessDenied();
        }
    }
}

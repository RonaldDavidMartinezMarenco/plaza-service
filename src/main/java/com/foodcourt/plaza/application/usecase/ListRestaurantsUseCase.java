package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.ListRestaurants;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Lists every restaurant for a {@link Role#CUSTOMER}.
 * There is no active flag on a restaurant, so the page is the whole catalog.
 */
@Service
public final class ListRestaurantsUseCase implements ListRestaurants {

    private final RestaurantRepository restaurants;

    public ListRestaurantsUseCase(RestaurantRepository restaurants) {
        this.restaurants = restaurants;
    }

    @Override
    public PageResult<RestaurantSummary> list(Caller caller, PageQuery query) {
        requireCustomer(caller);
        return restaurants.findPageOrderedByName(query);
    }

    private static void requireCustomer(Caller caller) {
        if (caller.role() != Role.CUSTOMER) {
            throw new AccessDenied();
        }
    }
}

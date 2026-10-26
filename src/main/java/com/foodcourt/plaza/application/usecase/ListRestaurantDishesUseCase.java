package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.ListRestaurantDishes;
import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.DishListQuery;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Lists the active dishes of one restaurant for a {@link Role#CUSTOMER}.
 * A category that fails the stored shape is rejected before the restaurant is read.
 */
@Service
public final class ListRestaurantDishesUseCase implements ListRestaurantDishes {

    private final RestaurantRepository restaurants;
    private final DishRepository dishes;

    public ListRestaurantDishesUseCase(RestaurantRepository restaurants, DishRepository dishes) {
        this.restaurants = restaurants;
        this.dishes = dishes;
    }

    @Override
    public PageResult<DishSummary> list(Caller caller, long restaurantId, PageQuery page, String category) {
        requireCustomer(caller);
        DishListQuery query = new DishListQuery(page, category);
        requireRestaurant(restaurantId);
        return dishes.findActivePage(restaurantId, query);
    }

    private static void requireCustomer(Caller caller) {
        if (caller.role() != Role.CUSTOMER) {
            throw new AccessDenied();
        }
    }

    private void requireRestaurant(long restaurantId) {
        if (restaurants.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFound();
        }
    }
}

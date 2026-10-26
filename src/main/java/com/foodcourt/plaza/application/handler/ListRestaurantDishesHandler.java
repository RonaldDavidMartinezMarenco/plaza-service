package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.ListRestaurantDishes;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link ListRestaurantDishes}.
 * This type reads the caller and forwards the call.
 */
@Service
public final class ListRestaurantDishesHandler {

    private final ListRestaurantDishes listRestaurantDishes;
    private final CurrentCaller currentCaller;

    public ListRestaurantDishesHandler(ListRestaurantDishes listRestaurantDishes, CurrentCaller currentCaller) {
        this.listRestaurantDishes = listRestaurantDishes;
        this.currentCaller = currentCaller;
    }

    public PageResult<DishSummary> list(long restaurantId, PageQuery page, String category) {
        return listRestaurantDishes.list(currentCaller.require(), restaurantId, page, category);
    }
}

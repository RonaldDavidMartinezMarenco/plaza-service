package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;

/**
 * Inbound port for the menu of one restaurant. The caller must be a customer.
 * {@code category} is the raw query value; {@code null} lists every active dish.
 */
public interface ListRestaurantDishes {

    PageResult<DishSummary> list(Caller caller, long restaurantId, PageQuery page, String category);
}

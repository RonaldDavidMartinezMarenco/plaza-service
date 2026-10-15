package com.foodcourt.plaza.application.handler;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.ListRestaurants;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.domain.spi.CurrentCaller;

/**
 * Entry the controller calls. The rules stay in {@link ListRestaurants}.
 * This type reads the caller and forwards the call.
 */
@Service
public final class ListRestaurantsHandler {

    private final ListRestaurants listRestaurants;
    private final CurrentCaller currentCaller;

    public ListRestaurantsHandler(ListRestaurants listRestaurants, CurrentCaller currentCaller) {
        this.listRestaurants = listRestaurants;
        this.currentCaller = currentCaller;
    }

    public PageResult<RestaurantSummary> list(PageQuery query) {
        return listRestaurants.list(currentCaller.require(), query);
    }
}

package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.RestaurantSummary;

/** Inbound port for listing restaurants. The caller must be a customer. */
public interface ListRestaurants {

    PageResult<RestaurantSummary> list(Caller caller, PageQuery query);
}

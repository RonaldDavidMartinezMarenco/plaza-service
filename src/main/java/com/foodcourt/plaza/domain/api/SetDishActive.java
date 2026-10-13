package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;

/** Inbound port for setting whether a dish is offered. The caller must own that restaurant. */
public interface SetDishActive {

    Dish setActive(Caller caller, long dishId, boolean active);
}

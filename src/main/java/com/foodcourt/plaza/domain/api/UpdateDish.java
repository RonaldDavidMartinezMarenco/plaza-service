package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;

/** Inbound port for replacing a dish's price and description. The caller must own that restaurant. */
public interface UpdateDish {

    Dish update(Caller caller, long dishId, DishUpdate change);
}

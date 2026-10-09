package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;

/** Inbound port for replacing a dish's price and description. */
public interface UpdateDish {

    Dish update(long dishId, DishUpdate change);
}

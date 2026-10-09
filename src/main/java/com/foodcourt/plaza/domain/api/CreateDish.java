package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;

/** Inbound port for creating a dish. */
public interface CreateDish {

    Dish create(NewDish dish);
}

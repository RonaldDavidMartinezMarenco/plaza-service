package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.NewDish;

/** Inbound port for creating a dish. The caller must own that restaurant. */
public interface CreateDish {

    Dish create(Caller caller, NewDish dish);
}

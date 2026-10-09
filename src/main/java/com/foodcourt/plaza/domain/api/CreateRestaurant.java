package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;

/** Inbound port for creating a restaurant. */
public interface CreateRestaurant {

    Restaurant create(NewRestaurant restaurant);
}

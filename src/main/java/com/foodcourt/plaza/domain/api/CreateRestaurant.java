package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;

/** Inbound port for creating a restaurant. The caller must be an administrator. */
public interface CreateRestaurant {

    Restaurant create(Caller caller, NewRestaurant restaurant);
}

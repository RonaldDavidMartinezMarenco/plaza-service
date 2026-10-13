package com.foodcourt.plaza.domain.api;

import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Restaurant;

/** Inbound port for reading one restaurant. The caller must be an owner or an administrator. */
public interface FindRestaurant {

    Restaurant find(Caller caller, long id);
}

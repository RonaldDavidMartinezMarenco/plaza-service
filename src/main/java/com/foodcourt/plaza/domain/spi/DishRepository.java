package com.foodcourt.plaza.domain.spi;

import com.foodcourt.plaza.domain.model.Dish;

/** Persistence port for {@code dish}. */
public interface DishRepository {

    Dish save(Dish dish);
}

package com.foodcourt.plaza.domain.spi;

import java.util.Optional;

import com.foodcourt.plaza.domain.model.Dish;

/** Persistence port for {@code dish}. */
public interface DishRepository {

    Optional<Dish> findById(long id);

    Dish save(Dish dish);
}

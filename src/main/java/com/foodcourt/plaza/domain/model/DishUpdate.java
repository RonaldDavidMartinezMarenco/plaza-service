package com.foodcourt.plaza.domain.model;

/**
 * Price and description sent to replace those fields on an existing dish.
 * Name, image, category, restaurant, and active are not part of this change.
 * The description may still have surrounding spaces; the use case trims it.
 */
public record DishUpdate(Integer price, String description) {
}

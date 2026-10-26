package com.foodcourt.plaza.domain.model;

/**
 * Page of a restaurant menu. {@code category} is null when the caller asked for every
 * active dish. Any other value is stored in the same shape as a created dish.
 */
public record DishListQuery(PageQuery page, String category) {

    public DishListQuery {
        if (category != null) {
            category = DishText.category(category).value();
        }
    }
}

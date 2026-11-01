package com.foodcourt.plaza.domain.model;

import java.util.List;

/**
 * Restaurant and lines as the client sent them.
 * Quantity, duplicates, and the menu are checked later.
 */
public record PlaceOrderDraft(Long restaurantId, List<OrderLineDraft> lines) {

    /** A line before {@link OrderQuantity} accepts it. */
    public record OrderLineDraft(Long dishId, Integer quantity) {
    }
}

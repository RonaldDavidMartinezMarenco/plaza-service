package com.foodcourt.plaza.infrastructure.output.jpa.entity;

/**
 * Closed projection of {@code restaurant}. Spring Data selects only these columns.
 */
public interface RestaurantSummaryView {

    Long getId();

    String getName();

    String getLogoUrl();
}

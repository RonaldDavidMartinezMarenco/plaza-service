package com.foodcourt.plaza.domain.model;

/** One dish and how many of it the customer asked for. The quantity is already checked. */
public record OrderLine(long dishId, int quantity) {
}

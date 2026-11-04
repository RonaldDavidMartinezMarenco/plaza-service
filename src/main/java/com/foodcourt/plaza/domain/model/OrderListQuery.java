package com.foodcourt.plaza.domain.model;

/** Page of orders for one exact status. The restaurant comes from the employee, not from this query. */
public record OrderListQuery(PageQuery page, OrderStatus status) {
}

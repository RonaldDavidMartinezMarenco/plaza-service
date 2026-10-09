package com.foodcourt.plaza.domain.model;

/** Account summary read from user-service. Only the id and the role are needed. */
public record Account(Long id, Role role) {
}

package com.foodcourt.plaza.infrastructure.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodcourt.plaza.infrastructure.output.jpa.entity.RestaurantEntity;

/** Spring Data access to {@code restaurant}. Used only by the JPA adapter. */
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {

    boolean existsByNit(String nit);

    boolean existsByNameIgnoreCase(String name);
}

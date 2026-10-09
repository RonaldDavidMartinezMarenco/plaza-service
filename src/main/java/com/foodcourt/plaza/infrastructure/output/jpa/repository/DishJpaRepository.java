package com.foodcourt.plaza.infrastructure.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodcourt.plaza.infrastructure.output.jpa.entity.DishEntity;

/** Spring Data access to {@code dish}. Used only by the JPA adapter. */
public interface DishJpaRepository extends JpaRepository<DishEntity, Long> {
}

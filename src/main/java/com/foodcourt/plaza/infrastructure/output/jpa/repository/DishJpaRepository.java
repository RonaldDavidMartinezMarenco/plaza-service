package com.foodcourt.plaza.infrastructure.output.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodcourt.plaza.infrastructure.output.jpa.entity.DishEntity;

/** Spring Data access to {@code dish}. Used only by the JPA adapter. */
public interface DishJpaRepository extends JpaRepository<DishEntity, Long> {

    /**
     * Bulk update, so {@code updatable = false} on the entity does not apply.
     * Returns the number of rows changed.
     */
    @Modifying
    @Query("update DishEntity d set d.active = :active where d.id = :id")
    int updateActive(@Param("id") long id, @Param("active") boolean active);

    Page<DishEntity> findByRestaurantIdAndActiveTrue(long restaurantId, Pageable pageable);

    Page<DishEntity> findByRestaurantIdAndActiveTrueAndCategory(
            long restaurantId, String category, Pageable pageable);
}

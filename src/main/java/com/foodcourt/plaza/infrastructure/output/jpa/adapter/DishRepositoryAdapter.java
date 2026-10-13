package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.spi.DishRepository;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.DishEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.mapper.DishJpaMapper;
import com.foodcourt.plaza.infrastructure.output.jpa.repository.DishJpaRepository;

/**
 * JPA adapter for {@link DishRepository}.
 * The class is not final: {@code @Transactional} needs a CGLIB subclass.
 */
@Service
public class DishRepositoryAdapter implements DishRepository {

    private final DishJpaRepository dishes;
    private final DishJpaMapper mapper = new DishJpaMapper();

    public DishRepositoryAdapter(DishJpaRepository dishes) {
        this.dishes = dishes;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dish> findById(long id) {
        return dishes.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Dish save(Dish dish) {
        DishEntity stored = dishes.saveAndFlush(mapper.toEntity(dish));
        return mapper.toDomain(stored);
    }

    @Override
    @Transactional
    public boolean updateActive(long id, boolean active) {
        return dishes.updateActive(id, active) == 1;
    }
}

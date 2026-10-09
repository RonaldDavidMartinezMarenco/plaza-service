package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.DishEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.repository.DishJpaRepository;

@ExtendWith(MockitoExtension.class)
class DishRepositoryAdapterTest {

    @Mock
    private DishJpaRepository dishes;

    private DishRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DishRepositoryAdapter(dishes);
    }

    @Test
    void saveReturnsTheStoredDish() {
        when(dishes.saveAndFlush(any(DishEntity.class))).thenAnswer(invocation -> {
            DishEntity row = invocation.getArgument(0);
            return new DishEntity(
                    21L,
                    row.getRestaurantId(),
                    row.getName(),
                    row.getPrice(),
                    row.getDescription(),
                    row.getImageUrl(),
                    row.getCategory(),
                    row.isActive());
        });

        Dish stored = adapter.save(draft());

        assertThat(stored.id()).isEqualTo(21L);
        assertThat(stored.restaurantId()).isEqualTo(4L);
        assertThat(stored.name()).isEqualTo("Pasta");
        assertThat(stored.price()).isEqualTo(12000);
        assertThat(stored.description()).isEqualTo("Cream sauce");
        assertThat(stored.imageUrl()).isEqualTo("https://cdn.example.com/dishes/pasta.png");
        assertThat(stored.category()).isEqualTo("Main");
        assertThat(stored.active()).isTrue();
    }

    private static Dish draft() {
        return new Dish(
                null,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                true);
    }
}

package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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
            DishEntity incoming = invocation.getArgument(0);
            return new DishEntity(
                    21L,
                    incoming.getRestaurantId(),
                    incoming.getName(),
                    incoming.getPrice(),
                    incoming.getDescription(),
                    incoming.getImageUrl(),
                    incoming.getCategory(),
                    incoming.isActive());
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

    @Test
    void findByIdReturnsTheStoredDish() {
        when(dishes.findById(21L)).thenReturn(Optional.of(row(false)));

        assertThat(adapter.findById(21L)).contains(new Dish(
                21L,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                false));
    }

    @Test
    void updateActiveReturnsTrueWhenOneRowChanges() {
        when(dishes.updateActive(21L, false)).thenReturn(1);

        assertThat(adapter.updateActive(21L, false)).isTrue();
        verify(dishes).updateActive(21L, false);
    }

    @Test
    void updateActiveReturnsFalseWhenNoRowChanges() {
        when(dishes.updateActive(21L, true)).thenReturn(0);

        assertThat(adapter.updateActive(21L, true)).isFalse();
        verify(dishes).updateActive(21L, true);
    }

    @Test
    void findByIdReturnsEmptyWhenTheDishIsMissing() {
        when(dishes.findById(21L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(21L)).isEmpty();
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

    private static DishEntity row(boolean active) {
        return new DishEntity(
                21L,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main",
                active);
    }
}

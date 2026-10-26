package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishListQuery;
import com.foodcourt.plaza.domain.model.DishSummary;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
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

    @Test
    void findActivePageReadsEveryActiveDishInNameOrder() {
        Pageable requested = PageRequest.of(0, 10, Sort.by("name", "id"));
        when(dishes.findByRestaurantIdAndActiveTrue(eq(4L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row(true)), requested, 10));

        PageResult<DishSummary> page = adapter.findActivePage(4L, new DishListQuery(new PageQuery(0, 10), null));

        ArgumentCaptor<Pageable> sent = ArgumentCaptor.forClass(Pageable.class);
        verify(dishes).findByRestaurantIdAndActiveTrue(eq(4L), sent.capture());
        verify(dishes, never()).findByRestaurantIdAndActiveTrueAndCategory(anyLong(), any(), any());
        assertThat(sent.getValue().getPageNumber()).isZero();
        assertThat(sent.getValue().getPageSize()).isEqualTo(10);
        assertThat(sent.getValue().getSort()).isEqualTo(Sort.by("name", "id"));
        assertThat(page.items()).containsExactly(summary());
        assertThat(page.totalItems()).isEqualTo(10);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void findActivePageFiltersByCategory() {
        Pageable requested = PageRequest.of(1, 5, Sort.by("name", "id"));
        when(dishes.findByRestaurantIdAndActiveTrueAndCategory(eq(1L), eq("Carnes"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row(true)), requested, 10));

        PageResult<DishSummary> page = adapter.findActivePage(1L, new DishListQuery(new PageQuery(1, 5), "Carnes"));

        ArgumentCaptor<Pageable> sent = ArgumentCaptor.forClass(Pageable.class);
        verify(dishes).findByRestaurantIdAndActiveTrueAndCategory(eq(1L), eq("Carnes"), sent.capture());
        verify(dishes, never()).findByRestaurantIdAndActiveTrue(anyLong(), any());
        assertThat(sent.getValue().getPageNumber()).isEqualTo(1);
        assertThat(sent.getValue().getPageSize()).isEqualTo(5);
        assertThat(sent.getValue().getSort()).isEqualTo(Sort.by("name", "id"));
        assertThat(page.items()).containsExactly(summary());
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.totalItems()).isEqualTo(10);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    @Test
    void findActivePageReturnsNoPagesWhenTheMenuIsEmpty() {
        when(dishes.findByRestaurantIdAndActiveTrue(eq(4L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        PageResult<DishSummary> page = adapter.findActivePage(4L, new DishListQuery(new PageQuery(0, 10), null));

        assertThat(page.items()).isEmpty();
        assertThat(page.totalItems()).isZero();
        assertThat(page.totalPages()).isZero();
    }

    private static DishSummary summary() {
        return new DishSummary(
                21L,
                4L,
                "Pasta",
                12000,
                "Cream sauce",
                "https://cdn.example.com/dishes/pasta.png",
                "Main");
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

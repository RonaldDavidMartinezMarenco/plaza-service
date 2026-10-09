package com.foodcourt.plaza.infrastructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.infrastructure.output.jpa.entity.RestaurantEntity;
import com.foodcourt.plaza.infrastructure.output.jpa.repository.RestaurantJpaRepository;

@ExtendWith(MockitoExtension.class)
class RestaurantRepositoryAdapterTest {

    @Mock
    private RestaurantJpaRepository restaurants;

    private RestaurantRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RestaurantRepositoryAdapter(restaurants);
    }

    @Test
    void saveReturnsTheStoredRestaurant() {
        when(restaurants.saveAndFlush(any(RestaurantEntity.class))).thenAnswer(invocation -> {
            RestaurantEntity row = invocation.getArgument(0);
            return new RestaurantEntity(
                    8L,
                    row.getName(),
                    row.getNit(),
                    row.getAddress(),
                    row.getPhone(),
                    row.getLogoUrl(),
                    row.getOwnerId());
        });

        Restaurant stored = adapter.save(draft());

        assertThat(stored.id()).isEqualTo(8L);
        assertThat(stored.name()).isEqualTo("Sushi 24");
        assertThat(stored.nit()).isEqualTo("900123456");
        assertThat(stored.ownerId()).isEqualTo(15L);
    }

    @Test
    void existsByNitDelegatesToTheRow() {
        when(restaurants.existsByNit("900123456")).thenReturn(true);

        assertThat(adapter.existsByNit("900123456")).isTrue();
        verify(restaurants).existsByNit("900123456");
    }

    @Test
    void findByIdReturnsTheRestaurant() {
        when(restaurants.findById(8L)).thenReturn(Optional.of(row(8L)));

        assertThat(adapter.findById(8L)).contains(draftWithId(8L));
    }

    @Test
    void findByIdReturnsEmptyWhenTheRowIsMissing() {
        when(restaurants.findById(8L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(8L)).isEmpty();
    }

    @Test
    void saveRejectsADuplicateNit() {
        when(restaurants.saveAndFlush(any(RestaurantEntity.class)))
                .thenThrow(unique("duplicate key value violates unique constraint \"restaurant_nit_key\""));

        assertThatThrownBy(() -> adapter.save(draft())).isInstanceOf(NitAlreadyRegistered.class);
    }

    @Test
    void saveRethrowsAUniqueViolationItCannotClassify() {
        DataIntegrityViolationException failure = unique("duplicate key value violates unique constraint \"other\"");
        when(restaurants.saveAndFlush(any(RestaurantEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(draft())).isSameAs(failure);
    }

    @Test
    void saveRethrowsAnIntegrityFailureThatIsNotUnique() {
        DataIntegrityViolationException failure = new DataIntegrityViolationException(
                "null",
                new SQLException("null value", "23502"));
        when(restaurants.saveAndFlush(any(RestaurantEntity.class))).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(draft())).isSameAs(failure);
    }

    private static DataIntegrityViolationException unique(String message) {
        return new DataIntegrityViolationException(
                "conflict",
                new SQLException(message, RestaurantRepositoryAdapter.UNIQUE_VIOLATION));
    }

    private static Restaurant draft() {
        return draftWithId(null);
    }

    private static Restaurant draftWithId(Long id) {
        return new Restaurant(
                id,
                "Sushi 24",
                "900123456",
                "Calle 10",
                "+573005698325",
                "https://cdn.example.com/logos/sushi.png",
                15L);
    }

    private static RestaurantEntity row(long id) {
        return new RestaurantEntity(
                id,
                "Sushi 24",
                "900123456",
                "Calle 10",
                "+573005698325",
                "https://cdn.example.com/logos/sushi.png",
                15L);
    }
}

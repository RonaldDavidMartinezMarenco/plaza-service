package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class FindRestaurantUseCaseTest {

    private static final Restaurant STORED =
            new Restaurant(8L, "Sushi 24", "900123456", "Calle 10", "+573005698325", "https://cdn.example.com/s.png", 15L);

    @Mock
    private RestaurantRepository restaurants;

    private FindRestaurantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindRestaurantUseCase(restaurants);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OWNER", "ADMINISTRATOR"})
    void findReturnsTheRestaurantToAnOwnerOrAnAdministrator(Role role) {
        when(restaurants.findById(8L)).thenReturn(Optional.of(STORED));

        Restaurant found = useCase.find(new Caller(99L, role), 8L);

        assertThat(found).isEqualTo(STORED);
    }

    @Test
    void findRejectsAMissingRestaurant() {
        when(restaurants.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.find(new Caller(15L, Role.OWNER), 8L))
                .isInstanceOf(RestaurantNotFound.class);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"EMPLOYEE", "CUSTOMER"})
    void findRejectsAnEmployeeOrACustomer(Role role) {
        assertThatThrownBy(() -> useCase.find(new Caller(15L, role), 8L))
                .isInstanceOf(AccessDenied.class);
        verifyNoInteractions(restaurants);
    }
}

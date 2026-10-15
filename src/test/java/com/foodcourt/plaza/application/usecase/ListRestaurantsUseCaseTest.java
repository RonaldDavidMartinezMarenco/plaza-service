package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.AccessDenied;
import com.foodcourt.plaza.domain.model.Caller;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class ListRestaurantsUseCaseTest {

    private static final PageQuery QUERY = new PageQuery(1, 10);
    private static final RestaurantSummary SUMMARY =
            new RestaurantSummary(8L, "7 Mares Cevichería", "https://images.example.com/logos/7-mares.png");

    @Mock
    private RestaurantRepository restaurants;

    private ListRestaurantsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListRestaurantsUseCase(restaurants);
    }

    @Test
    void listReturnsThePageFromTheRepository() {
        PageResult<RestaurantSummary> page = new PageResult<>(List.of(SUMMARY), 1, 10, 23);
        when(restaurants.findPageOrderedByName(QUERY)).thenReturn(page);

        PageResult<RestaurantSummary> result = useCase.list(new Caller(9L, Role.CUSTOMER), QUERY);

        assertThat(result).isEqualTo(page);
        verify(restaurants).findPageOrderedByName(QUERY);
    }

    @Test
    void listReturnsAnEmptyPage() {
        PageResult<RestaurantSummary> page = new PageResult<>(List.of(), 3, 10, 23);
        when(restaurants.findPageOrderedByName(QUERY)).thenReturn(page);

        PageResult<RestaurantSummary> result = useCase.list(new Caller(9L, Role.CUSTOMER), QUERY);

        assertThat(result).isEqualTo(page);
        assertThat(result.items()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "OWNER", "EMPLOYEE"})
    void listRejectsACallerThatIsNotACustomer(Role role) {
        assertThatThrownBy(() -> useCase.list(new Caller(2L, role), QUERY)).isInstanceOf(AccessDenied.class);
        verifyNoInteractions(restaurants);
    }
}

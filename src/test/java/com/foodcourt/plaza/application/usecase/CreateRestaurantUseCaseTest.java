package com.foodcourt.plaza.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;
import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class CreateRestaurantUseCaseTest {

    private static final long OWNER_ID = 15L;
    private static final String LOGO = "https://cdn.example.com/logos/sushi.png";

    @Mock
    private RestaurantRepository restaurants;

    @Mock
    private OwnerDirectory owners;

    private CreateRestaurantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateRestaurantUseCase(restaurants, owners);
    }

    @Test
    void createStoresATrimmedRestaurantForThatOwner() {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(restaurants.existsByNit("900123456")).thenReturn(false);
        when(restaurants.save(any(Restaurant.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 8L));

        Restaurant created = useCase.create(draft("  Sushi 24  ", "900123456"));

        ArgumentCaptor<Restaurant> stored = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurants).save(stored.capture());
        assertThat(stored.getValue().id()).isNull();
        assertThat(stored.getValue().name()).isEqualTo("Sushi 24");
        assertThat(stored.getValue().nit()).isEqualTo("900123456");
        assertThat(stored.getValue().address()).isEqualTo("Calle 10");
        assertThat(stored.getValue().phone()).isEqualTo("+573005698325");
        assertThat(stored.getValue().logoUrl()).isEqualTo(LOGO);
        assertThat(stored.getValue().ownerId()).isEqualTo(OWNER_ID);
        assertThat(created.id()).isEqualTo(8L);
        assertThat(created.name()).isEqualTo("Sushi 24");
    }

    @Test
    void createAcceptsASecondRestaurantForTheSameOwner() {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(restaurants.existsByNit("900123456")).thenReturn(false);
        when(restaurants.existsByNit("900123457")).thenReturn(false);
        when(restaurants.save(any(Restaurant.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 8L))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 9L));

        Restaurant first = useCase.create(draft("Sushi 24", "900123456"));
        Restaurant second = useCase.create(draft("Plaza-1", "900123457"));

        ArgumentCaptor<Restaurant> stored = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurants, times(2)).save(stored.capture());
        assertThat(stored.getAllValues()).extracting(Restaurant::ownerId).containsExactly(OWNER_ID, OWNER_ID);
        assertThat(stored.getAllValues()).extracting(Restaurant::nit).containsExactly("900123456", "900123457");
        assertThat(first.id()).isEqualTo(8L);
        assertThat(second.id()).isEqualTo(9L);
    }

    @Test
    void createRejectsANameThatIsOnlyDigits() {
        assertThatThrownBy(() -> useCase.create(draft("  123  ", "900123456")))
                .isInstanceOf(InvalidRestaurantName.class);
        verify(owners, never()).findById(anyLong());
        verify(restaurants, never()).save(any());
    }

    @Test
    void createRejectsAnUnknownOwner() {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(draft("Sushi 24", "900123456")))
                .isInstanceOf(OwnerNotFound.class);
        verify(restaurants, never()).existsByNit(any());
        verify(restaurants, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMINISTRATOR", "EMPLOYEE", "CUSTOMER"})
    void createRejectsAnAccountThatIsNotAnOwner(Role role) {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(new Account(OWNER_ID, role)));

        assertThatThrownBy(() -> useCase.create(draft("Sushi 24", "900123456")))
                .isInstanceOf(OwnerRoleRequired.class);
        verify(restaurants, never()).existsByNit(any());
        verify(restaurants, never()).save(any());
    }

    @Test
    void createRejectsADuplicateNit() {
        when(owners.findById(OWNER_ID)).thenReturn(Optional.of(owner()));
        when(restaurants.existsByNit("900123456")).thenReturn(true);

        assertThatThrownBy(() -> useCase.create(draft("Sushi 24", "900123456")))
                .isInstanceOf(NitAlreadyRegistered.class);
        verify(restaurants, never()).save(any());
    }

    private static Account owner() {
        return new Account(OWNER_ID, Role.OWNER);
    }

    private static NewRestaurant draft(String name, String nit) {
        return new NewRestaurant(name, nit, "Calle 10", "+573005698325", LOGO, OWNER_ID);
    }

    private static Restaurant withId(Restaurant restaurant, long id) {
        return new Restaurant(
                id,
                restaurant.name(),
                restaurant.nit(),
                restaurant.address(),
                restaurant.phone(),
                restaurant.logoUrl(),
                restaurant.ownerId());
    }
}

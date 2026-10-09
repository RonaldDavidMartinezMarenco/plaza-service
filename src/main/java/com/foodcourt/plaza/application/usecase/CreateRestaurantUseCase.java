package com.foodcourt.plaza.application.usecase;

import org.springframework.stereotype.Service;

import com.foodcourt.plaza.domain.api.CreateRestaurant;
import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.model.Account;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.RestaurantName;
import com.foodcourt.plaza.domain.model.Role;
import com.foodcourt.plaza.domain.spi.OwnerDirectory;
import com.foodcourt.plaza.domain.spi.RestaurantRepository;

/**
 * Creates a restaurant. Checks run in this order, and a rejection does not save:
 * name, owner account with role {@link Role#OWNER}, then a unique NIT.
 * The same owner may own more than one restaurant.
 */
@Service
public final class CreateRestaurantUseCase implements CreateRestaurant {

    private final RestaurantRepository restaurants;
    private final OwnerDirectory owners;

    public CreateRestaurantUseCase(RestaurantRepository restaurants, OwnerDirectory owners) {
        this.restaurants = restaurants;
        this.owners = owners;
    }

    @Override
    public Restaurant create(NewRestaurant draft) {
        RestaurantName name = requireName(draft);
        requireOwner(draft.ownerId());
        rejectDuplicateNit(draft.nit());
        return restaurants.save(stored(draft, name));
    }

    private static RestaurantName requireName(NewRestaurant draft) {
        return new RestaurantName(draft.name());
    }

    private void requireOwner(Long ownerId) {
        Account account = owners.findById(ownerId).orElseThrow(OwnerNotFound::new);
        if (account.role() != Role.OWNER) {
            throw new OwnerRoleRequired();
        }
    }

    private void rejectDuplicateNit(String nit) {
        if (restaurants.existsByNit(nit)) {
            throw new NitAlreadyRegistered();
        }
    }

    private static Restaurant stored(NewRestaurant draft, RestaurantName name) {
        return new Restaurant(
                null,
                name.value(),
                draft.nit(),
                draft.address(),
                draft.phone(),
                draft.logoUrl(),
                draft.ownerId());
    }
}

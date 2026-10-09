ALTER TABLE restaurant
    ADD CONSTRAINT restaurant_name_shape CHECK (
        char_length(name) BETWEEN 2 AND 100
        AND name = btrim(name)
        AND position('  ' IN name) = 0
    ),
    ADD CONSTRAINT restaurant_address_shape CHECK (
        char_length(address) BETWEEN 2 AND 200
        AND address = btrim(address)
        AND position('  ' IN address) = 0
    );

ALTER TABLE dish
    ADD CONSTRAINT dish_name_shape CHECK (
        char_length(name) BETWEEN 2 AND 100
        AND name = btrim(name)
        AND position('  ' IN name) = 0
    ),
    ADD CONSTRAINT dish_category_shape CHECK (
        char_length(category) BETWEEN 2 AND 50
        AND category = btrim(category)
        AND position('  ' IN category) = 0
    ),
    ADD CONSTRAINT dish_description_shape CHECK (
        char_length(description) BETWEEN 2 AND 500
        AND description = btrim(description)
        AND position('  ' IN description) = 0
    );

package com.foodcourt.plaza.infrastructure.input.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.foodcourt.plaza.application.handler.CreateDishHandler;
import com.foodcourt.plaza.application.handler.UpdateDishHandler;
import com.foodcourt.plaza.domain.exception.DishNotFound;
import com.foodcourt.plaza.domain.exception.InvalidDishDescription;
import com.foodcourt.plaza.domain.exception.InvalidDishPrice;
import com.foodcourt.plaza.domain.exception.InvalidDishText;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.Dish;
import com.foodcourt.plaza.domain.model.DishUpdate;
import com.foodcourt.plaza.domain.model.NewDish;
import com.foodcourt.plaza.infrastructure.exception.ApiExceptionHandler;
import com.foodcourt.plaza.infrastructure.security.SecurityConfig;

@WebMvcTest(DishController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class})
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateDishHandler handler;

    @MockitoBean
    private UpdateDishHandler updateDish;

    @BeforeEach
    void setUp() {
        reset(handler, updateDish);
    }

    @Test
    void createReturnsTheDish() throws Exception {
        when(handler.create(any(NewDish.class))).thenReturn(stored());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/dishes/21"))
                .andExpect(jsonPath("$.id").value(21))
                .andExpect(jsonPath("$.name").value("Pasta"))
                .andExpect(jsonPath("$.price").value(12000))
                .andExpect(jsonPath("$.description").value("Cream sauce"))
                .andExpect(jsonPath("$.imageUrl").value(image()))
                .andExpect(jsonPath("$.category").value("Main"))
                .andExpect(jsonPath("$.restaurantId").value(4))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createPassesTheRawTextToTheHandler() throws Exception {
        when(handler.create(any(NewDish.class))).thenReturn(stored());
        org.mockito.ArgumentCaptor<NewDish> sent = org.mockito.ArgumentCaptor.forClass(NewDish.class);

        postDish(validBody("  Pasta  ", "12000", "  Cream sauce  ", image(), "  Main  ", "4"))
                .andExpect(status().isCreated());

        verify(handler).create(sent.capture());
        assertThat(sent.getValue().name()).isEqualTo("  Pasta  ");
        assertThat(sent.getValue().description()).isEqualTo("  Cream sauce  ");
        assertThat(sent.getValue().category()).isEqualTo("  Main  ");
        assertThat(sent.getValue().restaurantId()).isEqualTo(4L);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedBodies")
    void createRejectsAnInvalidBody(String caseName, String body) throws Exception {
        postDish(body).andExpect(status().isBadRequest());
        verify(handler, never()).create(any());
    }

    @Test
    void createRejectsBlankText() throws Exception {
        when(handler.create(any(NewDish.class))).thenThrow(new InvalidDishText());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid dish text"))
                .andExpect(jsonPath("$.detail").value("The dish text cannot be blank"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createRejectsAnInvalidPrice() throws Exception {
        when(handler.create(any(NewDish.class))).thenThrow(new InvalidDishPrice());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid dish price"))
                .andExpect(jsonPath("$.detail").value("The dish price must be a positive integer"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createRejectsAnUnknownRestaurant() throws Exception {
        when(handler.create(any(NewDish.class))).thenThrow(new RestaurantNotFound());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Restaurant not found"))
                .andExpect(jsonPath("$.detail").value("No restaurant exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createRejectsAnAccountThatIsNotAnOwner() throws Exception {
        when(handler.create(any(NewDish.class))).thenThrow(new OwnerRoleRequired());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Owner role required"))
                .andExpect(jsonPath("$.detail").value("The account must have the owner role"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void createRejectsAnUnknownOwner() throws Exception {
        when(handler.create(any(NewDish.class))).thenThrow(new OwnerNotFound());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Owner not found"))
                .andExpect(jsonPath("$.detail").value("No account exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReportsAnUnavailableUserService() throws Exception {
        when(handler.create(any(NewDish.class))).thenThrow(new OwnerDirectoryUnavailable());

        postDish(validBody("Pasta", "12000", "Cream sauce", image(), "Main", "4"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User service unavailable"))
                .andExpect(jsonPath("$.detail").value("The user service is unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void updateReturnsTheDishWithTheNewPriceAndDescription() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenReturn(revised());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(21))
                .andExpect(jsonPath("$.name").value("Pasta"))
                .andExpect(jsonPath("$.price").value(15000))
                .andExpect(jsonPath("$.description").value("Tomato sauce"))
                .andExpect(jsonPath("$.imageUrl").value(image()))
                .andExpect(jsonPath("$.category").value("Main"))
                .andExpect(jsonPath("$.restaurantId").value(4))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updatePassesTheRawDescriptionToTheHandler() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenReturn(revised());
        org.mockito.ArgumentCaptor<DishUpdate> sent = org.mockito.ArgumentCaptor.forClass(DishUpdate.class);

        putDish(21L, updateBody("15000", "  Tomato sauce  ")).andExpect(status().isOk());

        verify(updateDish).update(eq(21L), sent.capture());
        assertThat(sent.getValue().price()).isEqualTo(15000);
        assertThat(sent.getValue().description()).isEqualTo("  Tomato sauce  ");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedUpdates")
    void updateRejectsAnInvalidBody(String caseName, String body) throws Exception {
        putDish(21L, body).andExpect(status().isBadRequest());
        verify(updateDish, never()).update(anyLong(), any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedIds")
    void updateRejectsAnInvalidId(String caseName, String id) throws Exception {
        mockMvc.perform(put("/dishes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("15000", "Tomato sauce")))
                .andExpect(status().isBadRequest());
        verify(updateDish, never()).update(anyLong(), any());
    }

    @Test
    void updateRejectsBlankText() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new InvalidDishText());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid dish text"))
                .andExpect(jsonPath("$.detail").value("The dish text cannot be blank"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRejectsADescriptionWithoutAWord() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new InvalidDishDescription());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid dish description"))
                .andExpect(jsonPath("$.detail").value("The description must include a word"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRejectsAnInvalidPrice() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new InvalidDishPrice());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid dish price"))
                .andExpect(jsonPath("$.detail").value("The dish price must be a positive integer"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRejectsAnUnknownDish() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new DishNotFound());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Dish not found"))
                .andExpect(jsonPath("$.detail").value("No dish exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateRejectsAnUnknownRestaurant() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new RestaurantNotFound());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Restaurant not found"))
                .andExpect(jsonPath("$.detail").value("No restaurant exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateRejectsAnAccountThatIsNotAnOwner() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new OwnerRoleRequired());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Owner role required"))
                .andExpect(jsonPath("$.detail").value("The account must have the owner role"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void updateRejectsAnUnknownOwner() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class))).thenThrow(new OwnerNotFound());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Owner not found"))
                .andExpect(jsonPath("$.detail").value("No account exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateReportsAnUnavailableUserService() throws Exception {
        when(updateDish.update(anyLong(), any(DishUpdate.class)))
                .thenThrow(new OwnerDirectoryUnavailable());

        putDish(21L, updateBody("15000", "Tomato sauce"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User service unavailable"))
                .andExpect(jsonPath("$.detail").value("The user service is unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    private static Stream<Arguments> rejectedUpdates() {
        return Stream.of(
                Arguments.of("missing price", updateMissingPrice()),
                Arguments.of("price zero", updateBody("0", "Tomato sauce")),
                Arguments.of("negative price", updateBody("-1", "Tomato sauce")),
                Arguments.of("null price", updateBody("null", "Tomato sauce")),
                Arguments.of("decimal price", updateBody("10.5", "Tomato sauce")),
                Arguments.of("blank description", updateBody("15000", "")),
                Arguments.of("spaces only description", updateBody("15000", "   ")),
                Arguments.of("digits only description", updateBody("15000", "12345")),
                Arguments.of("symbols only description", updateBody("15000", "!!!")),
                Arguments.of("description longer than 500", updateBody("15000", "a".repeat(501))),
                Arguments.of("unknown name", updateBodyWith("\"name\": \"Pasta\"")),
                Arguments.of("unknown image", updateBodyWith("\"imageUrl\": \"" + image() + "\"")),
                Arguments.of("unknown category", updateBodyWith("\"category\": \"Main\"")),
                Arguments.of("unknown restaurant", updateBodyWith("\"restaurantId\": 4")),
                Arguments.of("unknown active", updateBodyWith("\"active\": false")));
    }

    private static Stream<Arguments> rejectedIds() {
        return Stream.of(
                Arguments.of("id zero", "0"),
                Arguments.of("negative id", "-1"),
                Arguments.of("non numeric id", "abc"));
    }

    private static Stream<Arguments> rejectedBodies() {
        return Stream.of(
                Arguments.of("missing name", missingName()),
                Arguments.of("blank name", body("", "12000", "Cream sauce", image(), "Main", "4")),
                Arguments.of("spaces only name", body("   ", "12000", "Cream sauce", image(), "Main", "4")),
                Arguments.of("name longer than 100", body("a".repeat(101), "12000", "Cream sauce", image(), "Main", "4")),
                Arguments.of("price zero", body("Pasta", "0", "Cream sauce", image(), "Main", "4")),
                Arguments.of("negative price", body("Pasta", "-1", "Cream sauce", image(), "Main", "4")),
                Arguments.of("null price", body("Pasta", "null", "Cream sauce", image(), "Main", "4")),
                Arguments.of("decimal price", body("Pasta", "10.5", "Cream sauce", image(), "Main", "4")),
                Arguments.of("blank description", body("Pasta", "12000", "", image(), "Main", "4")),
                Arguments.of("digits only description", body("Pasta", "12000", "12345", image(), "Main", "4")),
                Arguments.of("symbols only description", body("Pasta", "12000", "!!!", image(), "Main", "4")),
                Arguments.of("description longer than 500", body("Pasta", "12000", "a".repeat(501), image(), "Main", "4")),
                Arguments.of("image without http", body("Pasta", "12000", "Cream sauce", "cdn.example.com/pasta.png", "Main", "4")),
                Arguments.of("blank category", body("Pasta", "12000", "Cream sauce", image(), "", "4")),
                Arguments.of("category longer than 50", body("Pasta", "12000", "Cream sauce", image(), "a".repeat(51), "4")),
                Arguments.of("null restaurant", body("Pasta", "12000", "Cream sauce", image(), "Main", "null")),
                Arguments.of("restaurant id zero", body("Pasta", "12000", "Cream sauce", image(), "Main", "0")),
                Arguments.of("negative restaurant id", body("Pasta", "12000", "Cream sauce", image(), "Main", "-1")));
    }

    private ResultActions postDish(String body) throws Exception {
        return mockMvc.perform(post("/dishes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions putDish(long id, String body) throws Exception {
        return mockMvc.perform(put("/dishes/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static String validBody(
            String name, String price, String description, String imageUrl, String category, String restaurantId) {
        return body(name, price, description, imageUrl, category, restaurantId);
    }

    private static String body(
            String name, String price, String description, String imageUrl, String category, String restaurantId) {
        return """
                {
                  "name": "%s",
                  "price": %s,
                  "description": "%s",
                  "imageUrl": "%s",
                  "category": "%s",
                  "restaurantId": %s
                }
                """.formatted(name, price, description, imageUrl, category, restaurantId);
    }

    private static String missingName() {
        return """
                {
                  "price": 12000,
                  "description": "Cream sauce",
                  "imageUrl": "%s",
                  "category": "Main",
                  "restaurantId": 4
                }
                """.formatted(image());
    }

    private static String updateBody(String price, String description) {
        return """
                {
                  "price": %s,
                  "description": "%s"
                }
                """.formatted(price, description);
    }

    private static String updateMissingPrice() {
        return """
                {
                  "description": "Tomato sauce"
                }
                """;
    }

    private static String updateBodyWith(String extraField) {
        return """
                {
                  "price": 15000,
                  "description": "Tomato sauce",
                  %s
                }
                """.formatted(extraField);
    }

    private static String image() {
        return "https://cdn.example.com/dishes/pasta.png";
    }

    private static Dish stored() {
        return new Dish(21L, 4L, "Pasta", 12000, "Cream sauce", image(), "Main", true);
    }

    private static Dish revised() {
        return new Dish(21L, 4L, "Pasta", 15000, "Tomato sauce", image(), "Main", true);
    }
}

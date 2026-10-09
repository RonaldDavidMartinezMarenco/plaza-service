package com.foodcourt.plaza.infrastructure.input.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.foodcourt.plaza.application.handler.CreateRestaurantHandler;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantAddress;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;
import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.RestaurantNameAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.infrastructure.exception.ApiExceptionHandler;
import com.foodcourt.plaza.infrastructure.security.SecurityConfig;

@WebMvcTest(RestaurantController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class})
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRestaurantHandler handler;

    @BeforeEach
    void setUp() {
        reset(handler);
    }

    @Test
    void createReturnsTheRestaurant() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenReturn(stored());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/restaurants/8"))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.name").value("Sushi 24"))
                .andExpect(jsonPath("$.nit").value("900123456"))
                .andExpect(jsonPath("$.address").value("Calle 10"))
                .andExpect(jsonPath("$.phone").value("+573005698325"))
                .andExpect(jsonPath("$.logoUrl").value(logo()))
                .andExpect(jsonPath("$.ownerId").value(15));
    }

    @Test
    void createPassesTheRawNameToTheHandler() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenReturn(stored());
        org.mockito.ArgumentCaptor<NewRestaurant> sent = org.mockito.ArgumentCaptor.forClass(NewRestaurant.class);

        postRestaurant(validBody("  Sushi 24  ", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isCreated());

        verify(handler).create(sent.capture());
        assertThat(sent.getValue().name()).isEqualTo("  Sushi 24  ");
        assertThat(sent.getValue().ownerId()).isEqualTo(15L);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedBodies")
    void createRejectsAnInvalidBody(String caseName, String body) throws Exception {
        postRestaurant(body).andExpect(status().isBadRequest());
        verify(handler, never()).create(any());
    }

    @Test
    void createRejectsAnInvalidRestaurantName() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new InvalidRestaurantName());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid restaurant name"))
                .andExpect(jsonPath("$.detail").value(
                        "The restaurant name must contain a letter and only the allowed characters"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createRejectsAnAccountThatIsNotAnOwner() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new OwnerRoleRequired());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Owner role required"))
                .andExpect(jsonPath("$.detail").value("The account must have the owner role"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void createRejectsAnUnknownOwner() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new OwnerNotFound());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Owner not found"))
                .andExpect(jsonPath("$.detail").value("No account exists for this id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createRejectsADuplicateNit() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new NitAlreadyRegistered());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("NIT already registered"))
                .andExpect(jsonPath("$.detail").value("NIT is already registered"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createRejectsADuplicateName() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new RestaurantNameAlreadyRegistered());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Restaurant name already registered"))
                .andExpect(jsonPath("$.detail").value("Restaurant name is already registered"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createRejectsAnInvalidAddress() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new InvalidRestaurantAddress());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid restaurant address"))
                .andExpect(jsonPath("$.detail").value(
                        "The address must contain a letter and only the allowed characters"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createReportsAnUnavailableUserService() throws Exception {
        when(handler.create(any(NewRestaurant.class))).thenThrow(new OwnerDirectoryUnavailable());

        postRestaurant(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User service unavailable"))
                .andExpect(jsonPath("$.detail").value("The user service is unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    private static Stream<Arguments> rejectedBodies() {
        return Stream.of(
                Arguments.of("name with only digits", body("123", "900123456", "Calle 10", "+573005698325", logo(), "15")),
                Arguments.of("name with only symbols", body("***", "900123456", "Calle 10", "+573005698325", logo(), "15")),
                Arguments.of("name with a sign", body("Sushi!", "900123456", "Calle 10", "+573005698325", logo(), "15")),
                Arguments.of("address with only symbols", body("Sushi 24", "900123456", "***", "+573005698325", logo(), "15")),
                Arguments.of("nit with letters", body("Sushi 24", "90012A", "Calle 10", "+573005698325", logo(), "15")),
                Arguments.of("phone longer than 13", body("Sushi 24", "900123456", "Calle 10", "+5730056983259", logo(), "15")),
                Arguments.of("phone with letters", body("Sushi 24", "900123456", "Calle 10", "57abc", logo(), "15")),
                Arguments.of("blank address", body("Sushi 24", "900123456", "", "+573005698325", logo(), "15")),
                Arguments.of("logo without http", body("Sushi 24", "900123456", "Calle 10", "+573005698325", "cdn.example.com/logo.png", "15")),
                Arguments.of("null owner", body("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "null")),
                Arguments.of("owner id zero", body("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "0")),
                Arguments.of("negative owner id", body("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "-1")));
    }

    private ResultActions postRestaurant(String body) throws Exception {
        return mockMvc.perform(post("/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private static String validBody(
            String name, String nit, String address, String phone, String logoUrl, String ownerId) {
        return body(name, nit, address, phone, logoUrl, ownerId);
    }

    private static String body(
            String name, String nit, String address, String phone, String logoUrl, String ownerId) {
        return """
                {
                  "name": "%s",
                  "nit": "%s",
                  "address": "%s",
                  "phone": "%s",
                  "logoUrl": "%s",
                  "ownerId": %s
                }
                """.formatted(name, nit, address, phone, logoUrl, ownerId);
    }

    private static String logo() {
        return "https://cdn.example.com/logos/sushi.png";
    }

    private static Restaurant stored() {
        return new Restaurant(8L, "Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), 15L);
    }
}

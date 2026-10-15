package com.foodcourt.plaza.infrastructure.input.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.foodcourt.plaza.application.handler.CreateRestaurantHandler;
import com.foodcourt.plaza.application.handler.FindRestaurantHandler;
import com.foodcourt.plaza.application.handler.ListRestaurantsHandler;
import com.foodcourt.plaza.domain.exception.InvalidPageRequest;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantAddress;
import com.foodcourt.plaza.domain.exception.InvalidRestaurantName;
import com.foodcourt.plaza.domain.exception.NitAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.RestaurantNameAlreadyRegistered;
import com.foodcourt.plaza.domain.exception.OwnerDirectoryUnavailable;
import com.foodcourt.plaza.domain.exception.OwnerNotFound;
import com.foodcourt.plaza.domain.exception.OwnerRoleRequired;
import com.foodcourt.plaza.domain.exception.RestaurantNotFound;
import com.foodcourt.plaza.domain.model.NewRestaurant;
import com.foodcourt.plaza.domain.model.PageQuery;
import com.foodcourt.plaza.domain.model.PageResult;
import com.foodcourt.plaza.domain.model.Restaurant;
import com.foodcourt.plaza.domain.model.RestaurantSummary;
import com.foodcourt.plaza.infrastructure.exception.ApiExceptionHandler;
import com.foodcourt.plaza.infrastructure.security.SecurityConfig;
import com.foodcourt.plaza.infrastructure.security.SecurityProblemHandlers;

@WebMvcTest(RestaurantController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class, SecurityProblemHandlers.class})
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRestaurantHandler handler;

    @MockitoBean
    private FindRestaurantHandler findHandler;

    @MockitoBean
    private ListRestaurantsHandler listHandler;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        reset(handler, findHandler, listHandler);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_OWNER", "ROLE_ADMINISTRATOR"})
    void findReturnsOnlyTheIdAndTheOwner(String authority) throws Exception {
        when(findHandler.find(8L)).thenReturn(stored());

        mockMvc.perform(get("/restaurants/8").with(jwt().authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.ownerId").value(15))
                .andExpect(jsonPath("$.nit").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.address").doesNotExist());
    }

    @Test
    void findRejectsAMissingRestaurant() throws Exception {
        when(findHandler.find(8L)).thenThrow(new RestaurantNotFound());

        mockMvc.perform(get("/restaurants/8").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Restaurant not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void findRejectsAnIdThatIsNotPositive(String id) throws Exception {
        mockMvc.perform(get("/restaurants/" + id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
                .andExpect(status().isBadRequest());
        verify(findHandler, never()).find(anyLong());
    }

    @Test
    void findRejectsAMissingToken() throws Exception {
        mockMvc.perform(get("/restaurants/8"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required"));
        verify(findHandler, never()).find(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_EMPLOYEE", "ROLE_CUSTOMER"})
    void findRejectsAnEmployeeOrACustomer(String authority) throws Exception {
        mockMvc.perform(get("/restaurants/8").with(jwt().authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You do not have permission for this action"));
        verify(findHandler, never()).find(anyLong());
    }

    @Test
    void listReturnsThePage() throws Exception {
        when(listHandler.list(new PageQuery(0, 10))).thenReturn(page(List.of(summary()), 0, 10, 23));

        mockMvc.perform(get("/restaurants").with(customer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(8))
                .andExpect(jsonPath("$.items[0].name").value("7 Mares Cevichería"))
                .andExpect(jsonPath("$.items[0].logoUrl").value("https://images.example.com/logos/7-mares.png"))
                .andExpect(jsonPath("$.items[0].nit").doesNotExist())
                .andExpect(jsonPath("$.items[0].phone").doesNotExist())
                .andExpect(jsonPath("$.items[0].address").doesNotExist())
                .andExpect(jsonPath("$.items[0].ownerId").doesNotExist())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(23))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void listUsesTheDefaultPageWhenTheQueryIsEmpty() throws Exception {
        when(listHandler.list(any(PageQuery.class))).thenReturn(page(List.of(), 0, 10, 0));
        org.mockito.ArgumentCaptor<PageQuery> sent = org.mockito.ArgumentCaptor.forClass(PageQuery.class);

        mockMvc.perform(get("/restaurants").with(customer())).andExpect(status().isOk());

        verify(listHandler).list(sent.capture());
        assertThat(sent.getValue()).isEqualTo(new PageQuery(0, 10));
    }

    @Test
    void listPassesTheRequestedPage() throws Exception {
        when(listHandler.list(any(PageQuery.class))).thenReturn(page(List.of(), 2, 5, 23));
        org.mockito.ArgumentCaptor<PageQuery> sent = org.mockito.ArgumentCaptor.forClass(PageQuery.class);

        mockMvc.perform(get("/restaurants").param("page", "2").param("size", "5").with(customer()))
                .andExpect(status().isOk());

        verify(listHandler).list(sent.capture());
        assertThat(sent.getValue()).isEqualTo(new PageQuery(2, 5));
    }

    @Test
    void listIgnoresASortParameter() throws Exception {
        when(listHandler.list(any(PageQuery.class))).thenReturn(page(List.of(summary()), 0, 10, 23));
        org.mockito.ArgumentCaptor<PageQuery> sent = org.mockito.ArgumentCaptor.forClass(PageQuery.class);

        mockMvc.perform(get("/restaurants").param("sort", "nit,desc").with(customer()))
                .andExpect(status().isOk());

        verify(listHandler).list(sent.capture());
        assertThat(sent.getValue()).isEqualTo(new PageQuery(0, 10));
    }

    @Test
    void listReturnsAnEmptyPage() throws Exception {
        when(listHandler.list(new PageQuery(3, 10))).thenReturn(page(List.of(), 3, 10, 23));

        mockMvc.perform(get("/restaurants").param("page", "3").param("size", "10").with(customer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(23))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @ParameterizedTest
    @CsvSource({
            "page, -1",
            "size, 0",
            "size, 51",
            "page, abc",
            "size, 1.5",
            "page, 2147483648"
    })
    void listRejectsAnInvalidPage(String name, String value) throws Exception {
        mockMvc.perform(get("/restaurants").param(name, value).with(customer()))
                .andExpect(status().isBadRequest());
        verify(listHandler, never()).list(any());
    }

    @Test
    void listRejectsAPageTheHandlerRefuses() throws Exception {
        when(listHandler.list(any(PageQuery.class))).thenThrow(new InvalidPageRequest());

        mockMvc.perform(get("/restaurants").with(customer()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid page request"))
                .andExpect(jsonPath("$.detail").value(
                        "The page must be zero or more, the size between 1 and 50, and page times size must not exceed 2147483647"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listRejectsAMissingToken() throws Exception {
        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required"));
        verify(listHandler, never()).list(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMINISTRATOR", "ROLE_OWNER", "ROLE_EMPLOYEE"})
    void listRejectsACallerThatIsNotACustomer(String authority) throws Exception {
        mockMvc.perform(get("/restaurants").with(jwt().authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You do not have permission for this action"));
        verifyNoInteractions(listHandler);
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

    @Test
    void createRejectsAMissingToken() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required"));
        verify(handler, never()).create(any());
    }

    @Test
    void createRejectsACallerThatIsNotAnAdministrator() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("Sushi 24", "900123456", "Calle 10", "+573005698325", logo(), "15")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("You do not have permission for this action"));
        verify(handler, never()).create(any());
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
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")))
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

    private static RestaurantSummary summary() {
        return new RestaurantSummary(8L, "7 Mares Cevichería", "https://images.example.com/logos/7-mares.png");
    }

    private static PageResult<RestaurantSummary> page(
            List<RestaurantSummary> items, int page, int size, long totalItems) {
        return new PageResult<>(items, page, size, totalItems);
    }

    private static RequestPostProcessor customer() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
}

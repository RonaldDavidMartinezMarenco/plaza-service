package com.foodcourt.plaza.infrastructure.output.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BearerTokenRelayTest {

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void copiesTheBearerTokenFromTheSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .subject("15")
                .claim("role", "OWNER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        RestClient.Builder builder = RestClient.builder().baseUrl("http://user-service");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.requestInterceptor(new BearerTokenRelay()).build();
        server.expect(requestTo("http://user-service/users/15"))
                .andExpect(header("Authorization", "Bearer signed-token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.get().uri("/users/15").retrieve().toBodilessEntity();

        server.verify();
    }
}

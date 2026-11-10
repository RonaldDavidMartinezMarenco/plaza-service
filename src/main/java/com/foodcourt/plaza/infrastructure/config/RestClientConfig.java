package com.foodcourt.plaza.infrastructure.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.foodcourt.plaza.infrastructure.output.client.BearerTokenRelay;

@Configuration
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration MESSAGING_READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    RestClient userServiceClient(ServiceClientsProperties clients, BearerTokenRelay bearerTokenRelay) {
        return client(clients.userServiceUrl(), bearerTokenRelay, READ_TIMEOUT);
    }

    @Bean
    RestClient messagingServiceClient(ServiceClientsProperties clients, BearerTokenRelay bearerTokenRelay) {
        return client(clients.messagingServiceUrl(), bearerTokenRelay, MESSAGING_READ_TIMEOUT);
    }

    @Bean
    RestClient traceabilityServiceClient(ServiceClientsProperties clients, BearerTokenRelay bearerTokenRelay) {
        return client(clients.traceabilityServiceUrl(), bearerTokenRelay, READ_TIMEOUT);
    }

    private static RestClient client(String baseUrl, BearerTokenRelay bearerTokenRelay, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .requestInterceptor(bearerTokenRelay)
                .build();
    }
}

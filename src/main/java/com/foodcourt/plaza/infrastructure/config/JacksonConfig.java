package com.foodcourt.plaza.infrastructure.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.MutableCoercionConfig;
import tools.jackson.databind.type.LogicalType;

/**
 * A JSON boolean stays a boolean. Text and numbers are not coerced, so
 * {@code "false"}, {@code 0}, and {@code 1} fail before a use case runs.
 */
@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer strictBooleans() {
        return builder -> builder.withCoercionConfig(LogicalType.Boolean, JacksonConfig::rejectTextAndNumbers);
    }

    private static void rejectTextAndNumbers(MutableCoercionConfig config) {
        config.setCoercion(CoercionInputShape.String, CoercionAction.Fail);
        config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
    }
}

package com.foodcourt.plaza.infrastructure.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.foodcourt.plaza.infrastructure.config.JwtConfig;

/** Maps the {@code role} claim to {@code ROLE_} authorities for {@code hasRole}. */
public final class JwtRoleConverter {

    private JwtRoleConverter() {
    }

    public static JwtAuthenticationConverter create() {
        JwtGrantedAuthoritiesConverter granted = new JwtGrantedAuthoritiesConverter();
        granted.setAuthoritiesClaimName(JwtConfig.ROLE_CLAIM);
        granted.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(granted);
        return converter;
    }
}

package com.tondise.utils.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@SuppressWarnings("removal")
public class KeycloakResolver implements AuthenticationManagerResolver<HttpServletRequest> {
    AuthenticationManagerResolver<HttpServletRequest> httpServletRequestAuthenticationManagerResolver;
    private final String jwtIssuerUri;

    @Override
    public AuthenticationManager resolve(HttpServletRequest context) {
        if (!Objects.nonNull(httpServletRequestAuthenticationManagerResolver)) {
            httpServletRequestAuthenticationManagerResolver = new JwtIssuerAuthenticationManagerResolver(jwtIssuerUri);
        }
        return httpServletRequestAuthenticationManagerResolver.resolve(context);
    }
}

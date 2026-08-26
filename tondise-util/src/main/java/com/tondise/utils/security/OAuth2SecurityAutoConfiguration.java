package com.tondise.utils.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class OAuth2SecurityAutoConfiguration {

    @Bean
    public KeycloakResolver keycloakResolver(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        return new KeycloakResolver(issuerUri);
    }
}

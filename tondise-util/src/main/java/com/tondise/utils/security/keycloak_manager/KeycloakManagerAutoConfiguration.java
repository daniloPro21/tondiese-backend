package com.tondise.utils.security.keycloak_manager;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties({KeycloakProperties.class})
@AutoConfiguration
public class KeycloakManagerAutoConfiguration {

    @Bean
    public ResteasyClient resteasyClient() {
        return new ResteasyClientBuilderImpl().connectTimeout(60, TimeUnit.SECONDS).build();
    }

    @Bean
    public KeycloakService keycloakService(KeycloakProvider keycloakProvider,
                                           KeycloakProperties keycloakProperties) {
        return new KeycloakServiceImpl(keycloakProvider, new KeycloakMapper(), keycloakProperties);
    }

    @Bean
    public KeycloakProvider keycloakProvider(
            KeycloakProperties keycloakProperties,
            ResteasyClient resteasyClient) {
        return new KeycloakProvider(keycloakProperties,resteasyClient);
    }
}

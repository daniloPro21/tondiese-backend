package com.tondise.utils.security.keycloak_manager;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@Data
@ConfigurationProperties(prefix = "tondise.identity-provider.keycloak")
public class KeycloakProperties {
    private String realm;
    private String authServerUrl;
    private String clientSecret;
    private String clientId;
    private String publicClientId;
}

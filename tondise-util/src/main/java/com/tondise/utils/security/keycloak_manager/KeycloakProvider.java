package com.tondise.utils.security.keycloak_manager;

import com.tondise.utils.security.keycloak_manager.data.TokenData;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class KeycloakProvider {
    private final KeycloakProperties properties;
    private final ResteasyClient resteasyClient;

    public Keycloak buildKeycloak() {
        return KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .serverUrl(properties.getAuthServerUrl())
                .realm(properties.getRealm())
                .clientSecret(properties.getClientSecret())
                .clientId(properties.getClientId())
                .resteasyClient(resteasyClient)
                .build();
    }

    public TokenData getAccessToken(String username, String password) {
        AccessTokenResponse accessToken = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.PASSWORD)
                .serverUrl(properties.getAuthServerUrl())
                .realm(properties.getRealm())
                .resteasyClient(resteasyClient)
                .clientId(properties.getPublicClientId())
                .username(username)
                .password(password)
                .build()
                .tokenManager()
                .getAccessToken();
        return createTokenData(accessToken);
    }

    public TokenData getAccessTokenForConfidentialClient(String clientId, String clientSecret) {
        AccessTokenResponse accessToken = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .serverUrl(properties.getAuthServerUrl())
                .realm(properties.getRealm())
                .resteasyClient(resteasyClient)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build()
                .tokenManager()
                .getAccessToken();
        return createTokenData(accessToken);
    }

    public TokenData getRefreshToken(String refreshToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // URL pour rafraîchir le token
            String tokenUrl = properties.getAuthServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";

            // Préparation des paramètres de la requête
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
            body.add("client_id", properties.getPublicClientId());
            body.add("client_secret", properties.getClientSecret());
            body.add("refresh_token", refreshToken);

            // Configuration des headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Construction de l'entité HTTP
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Appel du endpoint Keycloak
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

            // Analyse de la réponse
            Map<String, Object> responseBody = response.getBody();

            // Construction de l'objet TokenData
            return TokenData.builder()
                    .token((String) responseBody.get("access_token"))
                    .refreshToken((String) responseBody.get("refresh_token"))
                    .expiresIn((Integer) responseBody.get("expires_in"))
                    .refreshExpiresIn((Integer) responseBody.get("refresh_expires_in"))
                    .tokenType((String) responseBody.get("token_type"))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du rafraîchissement du token : " + e.getMessage(), e);
        }
    }

    // Helper method to build TokenData from AccessTokenResponse
    private TokenData createTokenData(AccessTokenResponse accessToken) {
        return TokenData.builder()
                .token(accessToken.getToken())
                .expiresIn(accessToken.getExpiresIn())
                .tokenType(accessToken.getTokenType())
                .refreshExpiresIn(accessToken.getRefreshExpiresIn())
                .refreshToken(accessToken.getRefreshToken())
                .build();
    }

    public void logOut(String userId) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .serverUrl(properties.getAuthServerUrl())
                .realm(properties.getRealm())
                .clientSecret(properties.getClientSecret())
                .clientId(properties.getClientId())
                .resteasyClient(resteasyClient)
                .build();
        keycloak.realm(properties.getRealm()).users().get(userId).logout();
    }

    public Optional<UserRepresentation> findByEmailExact(String email) {
        Keycloak keycloak = buildKeycloak();
        UsersResource users = keycloak.realm(properties.getRealm()).users();

        // Selon version, search(email, true) peut chercher sur plusieurs champs.
        // On filtre strictement sur email pour être sûr.
        List<UserRepresentation> candidates = users.searchByEmail(email, true);

        return candidates.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<String> findUserIdByEmail(String email) {
        return findByEmailExact(email).map(UserRepresentation::getId);
    }

}

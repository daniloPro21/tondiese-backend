package com.tondise.ecommerce.config.security;

import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.repository.UserRepository;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Résout l'utilisateur local (table {@code users}) à partir du JWT Keycloak
 * du principal courant. Le {@code sub} du token (l'ID utilisateur Keycloak)
 * est la clé de jointure, stockée sur {@link User#getKeycloakUserId()} depuis
 * l'inscription.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public User resolve(Jwt jwt) {
        return userRepository.findByKeycloakUserId(jwt.getSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable pour ce token"));
    }

    public UUID resolveUserId(Jwt jwt) {
        return resolve(jwt).getId();
    }
}

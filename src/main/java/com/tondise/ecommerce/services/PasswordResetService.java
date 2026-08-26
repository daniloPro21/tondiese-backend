package com.tondise.ecommerce.services;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Jeton de réinitialisation de mot de passe, stocké en Redis (TTL 1h) plutôt
 * qu'en colonne DB : Keycloak est désormais seul dépositaire du mot de passe,
 * ce jeton ne fait que prouver la possession de l'email le temps du parcours.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String KEY_PREFIX = "tondise:auth:reset-token:";

    private final StringRedisTemplate redisTemplate;

    public String createToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, userId.toString(), Duration.ofHours(1));
        return token;
    }

    public Optional<UUID> consumeToken(String token) {
        String key = KEY_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId == null) {
            return Optional.empty();
        }
        redisTemplate.delete(key);
        return Optional.of(UUID.fromString(userId));
    }
}

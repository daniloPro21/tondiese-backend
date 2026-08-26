package com.tondise.ecommerce.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tondise.utils.security.keycloak_manager.data.TokenData;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Cache Redis des tokens Keycloak par utilisateur, pour éviter un aller-retour
 * Keycloak complet à chaque requête de login tant que le token est valide
 * (même logique que {@code TokenSessionService} chez task-force-remita).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private static final String KEY_PREFIX = "tondise:auth:session:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveToken(UUID userId, TokenData tokenData) {
        try {
            String json = objectMapper.writeValueAsString(tokenData);
            redisTemplate.opsForValue().set(KEY_PREFIX + userId, json, Duration.ofSeconds(tokenData.expiresIn()));
        } catch (Exception e) {
            log.warn("Impossible de mettre en cache le token pour {} : {}", userId, e.getMessage());
        }
    }

    public Optional<TokenData> getValidToken(UUID userId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, TokenData.class));
        } catch (Exception e) {
            log.warn("Token en cache illisible pour {} : {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public void invalidate(UUID userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}

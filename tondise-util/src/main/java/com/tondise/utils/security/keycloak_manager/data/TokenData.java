package com.tondise.utils.security.keycloak_manager.data;

import lombok.Builder;

@Builder
public record TokenData(String token, long expiresIn, long refreshExpiresIn, String tokenType, String refreshToken) {
}

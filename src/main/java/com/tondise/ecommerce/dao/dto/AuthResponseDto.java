package com.tondise.ecommerce.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pas d'entité "AuthResponse" en base : ce DTO n'enveloppe que la réponse
 * login/register (utilisateur + tokens), il n'étend donc pas AbstractDTO —
 * contrairement aux DTOs qui reflètent une entité gérée en CRUD.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private UserDto user;
    private String token;
    private String refreshToken;
}

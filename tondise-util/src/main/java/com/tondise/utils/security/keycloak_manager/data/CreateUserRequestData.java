package com.tondise.utils.security.keycloak_manager.data;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateUserRequestData(
        String firstName,
        String lastName,
        String email,
        String username,
        String password,
        @NotNull Boolean isActive,
        List<RoleData> roles
) {
}

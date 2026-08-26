package com.tondise.utils.security.keycloak_manager;

import com.tondise.utils.security.keycloak_manager.data.CreateUserRequestData;
import com.tondise.utils.security.keycloak_manager.data.RoleData;
import com.tondise.utils.security.keycloak_manager.data.TokenData;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Optional;

public interface KeycloakService {
    String createKeycloakUser(CreateUserRequestData data);

    TokenData getAccessToken(String username, String password);

    void addRoleToUser(String userId, List<RoleData> roleName);

    void resetPassword(String userId, String newPassword);

    TokenData getRefreshToken(String refrshToken);

    void logOut(String userId);

    Optional<String> findUserIdByEmail(String email);

    Optional<UserRepresentation>  getUserrepresentative(String email);

}

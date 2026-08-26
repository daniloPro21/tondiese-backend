package com.tondise.utils.security.keycloak_manager;

import com.tondise.utils.cache.LocalCache;
import com.tondise.utils.exception.KeycloakUserCreationFailedException;
import com.tondise.utils.security.keycloak_manager.data.CreateUserRequestData;
import com.tondise.utils.security.keycloak_manager.data.RoleData;
import com.tondise.utils.security.keycloak_manager.data.TokenData;
import com.tondise.utils.security.keycloak_manager.exception.UsernameOrPasswordIsBadException;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Getter
public class KeycloakServiceImpl implements KeycloakService {
    private final KeycloakProvider keycloakProvider;
    private final KeycloakMapper mapper;
    private final KeycloakProperties properties;

    @PostConstruct
    public void init() {
        LocalCache.Instance.init();
    }

    private KeycloakFetcherAdapter getFreshFetcher() {
        return new KeycloakFetcherAdapter(getRealmResource(), properties.getPublicClientId());
    }

    private RealmResource getRealmResource() {
        return keycloakProvider.buildKeycloak().realm(properties.getRealm());
    }

    @Override
    public String createKeycloakUser(CreateUserRequestData data) {
        UsersResource usersResource = getRealmResource().users();
        try (Response response = usersResource.create(mapper.mapToUserRepresentation(data))) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new KeycloakUserCreationFailedException(
                        "Keycloak a refusé la création (HTTP " + response.getStatus() + ")");
            }
            // L'id est lu depuis l'en-tête Location de la réponse de création, jamais par une
            // recherche par username juste après : cette dernière est sujette à un délai de
            // propagation dans l'index de recherche Keycloak et peut ne pas encore voir
            // l'utilisateur qui vient d'être créé (échec systématique observé en pratique).
            String userId = CreatedResponseUtil.getCreatedId(response);
            addRoleToUser(userId, data.roles());
            setPassword(data.password(), usersResource.get(userId));
            return userId;
        } catch (KeycloakUserCreationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakUserCreationFailedException(e.getMessage());
        }
    }

    @Override
    public TokenData getAccessToken(String username, String password) {
        try {
            return keycloakProvider.getAccessToken(username, password);
        } catch (NotAuthorizedException e) {
            throw new UsernameOrPasswordIsBadException();
        }
    }

    @Override
    public void addRoleToUser(String userId, List<RoleData> roles) {
        Optional.ofNullable(getRealmResource())
                .map(RealmResource::users)
                .map(usersResource -> usersResource.get(userId))
                .map(UserResource::roles)
                .map(roleMappingResource -> roleMappingResource.clientLevel(getFreshFetcher().getClientId()))
                .ifPresent(roleScopeResource -> roleScopeResource.add(getFreshFetcher().getRoleRepresentation(roles)));
    }

    @Override
    public void resetPassword(String userId, String newPassword) {
        Optional.ofNullable(getRealmResource())
                .map(RealmResource::users)
                .map(usersResource -> usersResource.get(userId))
                .ifPresent(userResource -> setPassword(newPassword, userResource));
    }

    private void setPassword(String password, UserResource userResource) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setValue(password);
        userResource.resetPassword(credentialRepresentation);
    }

    @Override
    public TokenData getRefreshToken(String refrshToken) {
        return keycloakProvider.getRefreshToken(refrshToken);
    }

    @Override
    public void logOut(String userId) {
        keycloakProvider.logOut(userId);
    }

    @Override
    public Optional<String> findUserIdByEmail(String email) {
        return keycloakProvider.findUserIdByEmail(email);
    }

    @Override
    public Optional<UserRepresentation> getUserrepresentative(String email) {
        return keycloakProvider.findByEmailExact(email);
    }

}
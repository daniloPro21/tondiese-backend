package com.tondise.utils.security.keycloak_manager;

import com.tondise.utils.security.keycloak_manager.data.RoleData;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class KeycloakFetcherAdapter {
    private final RealmResource realmResource;
    private final String publicClientId;

    public List<RoleRepresentation> getRoleRepresentation(List<RoleData> roles) {
        List<String> roleNames = Optional.ofNullable(roles)
                .orElse(List.of())
                .stream()
                .map(RoleData::name)
                .toList();

        List<ClientRepresentation> clients = realmResource.clients().findAll();

        String clientId = clients.stream()
                .filter(client -> Objects.equals(client.getClientId(), publicClientId))
                .findFirst()
                .map(ClientRepresentation::getId)
                .orElseThrow();

        return realmResource.clients().get(clientId)
                .roles()
                .list()
                .stream()
                .filter(role -> roleNames.contains(role.getName()))
                .toList();
    }

    public String getClientId() {
        return realmResource.clients().findAll()
                .stream()
                .filter(client -> Objects.equals(client.getClientId(), publicClientId))
                .findFirst()
                .map(ClientRepresentation::getId)
                .orElseThrow();
    }
}

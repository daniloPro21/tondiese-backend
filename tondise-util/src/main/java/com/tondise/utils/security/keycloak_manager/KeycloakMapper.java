package com.tondise.utils.security.keycloak_manager;

import com.tondise.utils.security.keycloak_manager.data.CreateUserRequestData;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Objects;


public class KeycloakMapper {
    public KeycloakMapper() {
    }

    public UserRepresentation mapToUserRepresentation(CreateUserRequestData data) {
        if (data == null) {
            return null;
        } else {
            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(data.username());
            userRepresentation.setEmail(Objects.nonNull(data.email()) ? data.email() : "");
            userRepresentation.setLastName(Objects.nonNull(data.lastName()) ? data.lastName() : "");
            userRepresentation.setFirstName(Objects.nonNull(data.firstName()) ? data.firstName() : "");
            userRepresentation.setEnabled(data.isActive());
            return userRepresentation;
        }
    }

}

package com.tondise.ecommerce.dao.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    private String postalCode;

    @NotBlank
    private String country;

    @NotBlank
    private String phone;

    /**
     * Nom JSON forcé : le getter Lombok {@code isDefault()} (champ déjà préfixé {@code is})
     * fait dériver par Jackson la propriété {@code "default"}, pas {@code "isDefault"} — un
     * payload client {@code {"isDefault": true}} serait sinon silencieusement ignoré.
     */
    @JsonProperty("isDefault")
    private boolean isDefault;
}

package com.tondise.ecommerce.dao.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tondise.utils.abstractModel.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto extends AbstractDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String phone;
    /** Voir {@code AddressRequest.isDefault} : le nom JSON doit être forcé, Jackson dériverait sinon {@code "default"}. */
    @JsonProperty("isDefault")
    private boolean isDefault;
}

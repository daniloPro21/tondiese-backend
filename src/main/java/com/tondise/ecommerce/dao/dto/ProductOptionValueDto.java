package com.tondise.ecommerce.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionValueDto {
    private UUID id;
    private String label;
    private String value;
    private BigDecimal priceModifier;
    /** Nom JSON forcé : Jackson dériverait sinon "default" depuis le getter isDefault(). */
    @JsonProperty("isDefault")
    private boolean isDefault;
}

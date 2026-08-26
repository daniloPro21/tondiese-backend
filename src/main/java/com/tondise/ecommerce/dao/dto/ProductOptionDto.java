package com.tondise.ecommerce.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tondise.ecommerce.dao.enums.ProductOptionType;
import java.util.List;
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
public class ProductOptionDto {
    private UUID id;
    private String name;
    private ProductOptionType type;
    /** Nom JSON forcé : Jackson dériverait sinon "required" depuis le getter isRequired(). */
    @JsonProperty("isRequired")
    private boolean isRequired;
    private List<ProductOptionValueDto> values;
}

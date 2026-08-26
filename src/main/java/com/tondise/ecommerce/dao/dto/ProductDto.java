package com.tondise.ecommerce.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tondise.utils.abstractModel.AbstractDTO;
import java.math.BigDecimal;
import java.util.List;
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
public class ProductDto extends AbstractDTO {
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private BigDecimal basePrice;
    private String mainImage;
    private List<String> images;
    private CategoryDto category;
    private List<ProductOptionDto> options;
    private List<PricingTierDto> pricingTiers;
    /** Nom JSON forcé : Jackson dériverait sinon "featured" depuis le getter isFeatured(). */
    @JsonProperty("isFeatured")
    private boolean isFeatured;
}

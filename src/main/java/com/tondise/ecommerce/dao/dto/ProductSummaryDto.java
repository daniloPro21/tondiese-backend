package com.tondise.ecommerce.dao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
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
public class ProductSummaryDto {
    private UUID id;
    private String name;
    private String slug;
    private String shortDescription;
    private BigDecimal basePrice;
    private String mainImage;
    private List<String> images;
    /** Nom JSON forcé : Jackson dériverait sinon "featured" depuis le getter isFeatured(). */
    @JsonProperty("isFeatured")
    private boolean isFeatured;
}

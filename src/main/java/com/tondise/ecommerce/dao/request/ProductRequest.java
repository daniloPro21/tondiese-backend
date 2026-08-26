package com.tondise.ecommerce.dao.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String shortDescription;

    private String description;

    @NotNull
    private BigDecimal basePrice;

    private String mainImage;

    private List<String> images;

    private UUID categoryId;

    /** Nom JSON forcé : Jackson dériverait sinon "featured" depuis le getter isFeatured(). */
    @JsonProperty("isFeatured")
    private boolean isFeatured;

    private Integer stockQuantity;
}

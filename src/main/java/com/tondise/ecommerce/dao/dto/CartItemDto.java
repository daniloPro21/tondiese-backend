package com.tondise.ecommerce.dao.dto;

import java.math.BigDecimal;
import java.util.Map;
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
public class CartItemDto {
    private UUID id;
    private ProductSummaryDto product;
    private Integer quantity;
    private Map<UUID, UUID> selectedOptions;
    private UUID designId;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

package com.tondise.ecommerce.dao.dto;

import java.math.BigDecimal;
import java.util.Map;
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
public class PriceCalculationDto {
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal optionsPrice;
    private BigDecimal subtotal;
    private Map<String, BigDecimal> breakdown;
}

package com.tondise.ecommerce.dao.dto;

import java.math.BigDecimal;
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
public class PaymentIntentDto {
    private String id;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
}

package com.tondise.ecommerce.dao.dto;

import java.util.List;
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
public class PaymentConfigDto {
    private String publishableKey;
    private String defaultGateway;
    private List<String> availableGateways;
}

package com.tondise.ecommerce.dao.dto;

import com.tondise.utils.abstractModel.AbstractDTO;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Volontairement sans {@code providerPaymentId} ni {@code clientSecret} :
 * ces champs sensibles ne sont retournés qu'une fois, à la création, via
 * {@link PaymentIntentDto} — jamais par une lecture générique (id deviné,
 * liste...).
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDto extends AbstractDTO {
    private UUID orderId;
    private String gateway;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String phoneNumber;
    private String failureMessage;
}

package com.tondise.ecommerce.dao.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmPaymentRequest {

    @NotBlank
    private String paymentIntentId;

    private String gateway;
}

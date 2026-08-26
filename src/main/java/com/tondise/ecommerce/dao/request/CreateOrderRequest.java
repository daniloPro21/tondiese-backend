package com.tondise.ecommerce.dao.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private UUID shippingAddressId;

    private UUID billingAddressId;

    @NotBlank
    private String shippingMethod;

    @NotBlank
    private String paymentMethod;

    private String promoCode;

    private String notes;
}

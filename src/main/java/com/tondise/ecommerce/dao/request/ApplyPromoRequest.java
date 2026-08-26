package com.tondise.ecommerce.dao.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyPromoRequest {

    @NotBlank
    private String code;
}

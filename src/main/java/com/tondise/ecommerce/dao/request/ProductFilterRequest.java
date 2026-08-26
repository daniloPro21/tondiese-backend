package com.tondise.ecommerce.dao.request;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterRequest {

    private UUID categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
    private int page = 0;
    private int perPage = 20;
}

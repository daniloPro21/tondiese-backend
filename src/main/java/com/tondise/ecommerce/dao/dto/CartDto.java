package com.tondise.ecommerce.dao.dto;

import com.tondise.utils.abstractModel.AbstractDTO;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto extends AbstractDTO {
    private List<CartItemDto> items;
    private int itemsCount;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private String promoCode;
    private BigDecimal total;
}

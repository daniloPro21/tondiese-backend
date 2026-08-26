package com.tondise.ecommerce.dao.dto;

import com.tondise.utils.abstractModel.AbstractDTO;
import java.math.BigDecimal;
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
public class OrderDto extends AbstractDTO {
    private String orderNumber;
    private OrderStatusDto status;
    private String paymentStatus;
    private String shippingMethod;
    private BigDecimal shippingCost;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private String promoCode;
    private String trackingNumber;
}

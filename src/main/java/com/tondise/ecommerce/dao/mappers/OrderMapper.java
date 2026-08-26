package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.OrderDto;
import com.tondise.ecommerce.dao.dto.OrderStatusDto;
import com.tondise.ecommerce.dao.enums.OrderStatus;
import com.tondise.ecommerce.dao.models.Order;
import com.tondise.ecommerce.dao.request.CreateOrderRequest;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface OrderMapper extends BaseMapper<Order, OrderDto> {

    @Mapping(target = "status", expression = "java(toStatusDto(order.getStatus()))")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    OrderDto toDto(Order order);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    Order toModel(OrderDto dto);

    /**
     * Champs directement mappables uniquement (shippingMethod, notes) : le
     * reste (adresses, totaux, numéro de commande) exige une résolution par
     * repository et un contexte utilisateur — {@link com.tondise.ecommerce.services.OrderService#createOrder}
     * construit la commande à la main, comme dans task-force-remita.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "shippingCost", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    Order toModelRequest(CreateOrderRequest request);

    List<OrderDto> toDtoList(List<Order> orders);

    default OrderStatusDto toStatusDto(OrderStatus status) {
        return OrderStatusDto.builder()
                .value(status.name())
                .label(status.getLabel())
                .color(status.getColor())
                .build();
    }
}

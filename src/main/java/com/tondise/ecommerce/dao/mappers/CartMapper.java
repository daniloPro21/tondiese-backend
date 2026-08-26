package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.CartDto;
import com.tondise.ecommerce.dao.dto.CartItemDto;
import com.tondise.ecommerce.dao.models.Cart;
import com.tondise.ecommerce.dao.models.CartItem;
import com.tondise.ecommerce.dao.request.CartRequest;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = ProductMapper.class)
public interface CartMapper extends BaseMapper<Cart, CartDto> {

    CartItemDto toItemDto(CartItem item);

    List<CartItemDto> toItemDtoList(List<CartItem> items);

    /**
     * Mapping structurel seul (id/items/promoCode) : {@code subtotal}/{@code
     * discount}/{@code total} dépendent du calcul de promo et restent calculés
     * par {@code CartService.toDto}, la seule implémentation réellement utilisée
     * pour construire la réponse de l'API.
     */
    @Mapping(target = "itemsCount", expression = "java(cart.getItems().size())")
    @Mapping(target = "promoCode", source = "promoCode.code")
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "total", ignore = true)
    CartDto toDto(Cart cart);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "items", ignore = true)
    Cart toModel(CartDto dto);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "items", ignore = true)
    Cart toModelRequest(CartRequest request);
}

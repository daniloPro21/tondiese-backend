package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface CartItemRepository extends BaseRepository<CartItem> {

    Optional<CartItem> findByIdAndCartId(UUID id, UUID cartId);

    List<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
}

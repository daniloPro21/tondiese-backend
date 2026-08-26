package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.Cart;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface CartRepository extends BaseRepository<Cart> {

    Optional<Cart> findByUserId(UUID userId);
}

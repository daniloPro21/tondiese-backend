package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tondise.utils.abstractModel.BaseRepository;

public interface OrderRepository extends BaseRepository<Order> {

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    Optional<Order> findByOrderNumber(String orderNumber);
}

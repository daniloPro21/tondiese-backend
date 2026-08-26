package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepository extends BaseRepository<Favorite> {

    List<Favorite> findByUserId(UUID userId);

    Page<Favorite> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    Optional<Favorite> findByIdAndUserId(UUID id, UUID userId);

    Optional<Favorite> findByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}

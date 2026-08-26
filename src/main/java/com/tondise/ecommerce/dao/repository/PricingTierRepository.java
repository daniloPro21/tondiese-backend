package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.PricingTier;
import java.util.List;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface PricingTierRepository extends BaseRepository<PricingTier> {

    List<PricingTier> findByProductIdOrderByQuantityAsc(UUID productId);
}

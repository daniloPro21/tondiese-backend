package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.PromoCode;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface PromoCodeRepository extends BaseRepository<PromoCode> {

    Optional<PromoCode> findByCodeIgnoreCaseAndActiveTrue(String code);
}

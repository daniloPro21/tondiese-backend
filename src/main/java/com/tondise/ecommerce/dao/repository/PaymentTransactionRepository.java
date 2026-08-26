package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.models.PaymentTransaction;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction> {

    Optional<PaymentTransaction> findByProviderPaymentId(String providerPaymentId);

    Optional<PaymentTransaction> findByIdAndOrder_UserId(UUID id, UUID userId);

    Page<PaymentTransaction> findByOrder_UserId(UUID userId, Pageable pageable);

    long countByOrder_UserId(UUID userId);
}

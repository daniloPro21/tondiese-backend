package com.tondise.ecommerce.dao.repository;

import com.tondise.ecommerce.dao.enums.PaymentGateway;
import com.tondise.ecommerce.dao.models.PaymentGatewayConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tondise.utils.abstractModel.BaseRepository;

public interface PaymentGatewayConfigRepository extends BaseRepository<PaymentGatewayConfig> {

    List<PaymentGatewayConfig> findByEnabledTrue();

    Optional<PaymentGatewayConfig> findByGateway(PaymentGateway gateway);

    Optional<PaymentGatewayConfig> findByIsDefaultTrue();
}

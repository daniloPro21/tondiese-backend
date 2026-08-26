package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.PaymentTransactionDto;
import com.tondise.ecommerce.dao.models.PaymentTransaction;
import com.tondise.ecommerce.dao.request.PaymentTransactionRequest;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface PaymentTransactionMapper extends BaseMapper<PaymentTransaction, PaymentTransactionDto> {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "gateway", expression = "java(transaction.getGateway().name())")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    PaymentTransactionDto toDto(PaymentTransaction transaction);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    @Mapping(target = "status", ignore = true)
    PaymentTransaction toModel(PaymentTransactionDto dto);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "providerPaymentId", ignore = true)
    @Mapping(target = "clientSecret", ignore = true)
    @Mapping(target = "amount", ignore = true)
    PaymentTransaction toModelRequest(PaymentTransactionRequest request);
}

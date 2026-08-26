package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.GatewayConfigDto;
import com.tondise.ecommerce.dao.dto.PaymentConfigDto;
import com.tondise.ecommerce.dao.dto.PaymentConfirmResultDto;
import com.tondise.ecommerce.dao.dto.PaymentIntentDto;
import com.tondise.ecommerce.dao.dto.PaymentMethodDto;
import com.tondise.ecommerce.dao.dto.PaymentTransactionDto;
import com.tondise.ecommerce.dao.enums.PaymentGateway;
import com.tondise.ecommerce.dao.enums.PaymentStatus;
import com.tondise.ecommerce.dao.mappers.PaymentTransactionMapper;
import com.tondise.ecommerce.dao.models.Order;
import com.tondise.ecommerce.dao.models.PaymentGatewayConfig;
import com.tondise.ecommerce.dao.models.PaymentTransaction;
import com.tondise.ecommerce.dao.repository.OrderRepository;
import com.tondise.ecommerce.dao.repository.PaymentGatewayConfigRepository;
import com.tondise.ecommerce.dao.repository.PaymentTransactionRepository;
import com.tondise.ecommerce.dao.request.PaymentTransactionRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Squelette d'intégration paiement. La création réelle du PaymentIntent chez Stripe et
 * l'appel aux API Mobile Money (MTN/Orange) restent à brancher — voir TODO ci-dessous.
 */
@Service
@Transactional
public class PaymentService extends AbstractService<PaymentTransaction, PaymentTransactionDto, PaymentTransactionRequest> {

    private final PaymentGatewayConfigRepository gatewayConfigRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final PaymentTransactionMapper paymentTransactionMapper;

    @Value("${app.payments.stripe.publishable-key:}")
    private String stripePublishableKey;

    public PaymentService(PaymentGatewayConfigRepository gatewayConfigRepository,
                           PaymentTransactionRepository paymentTransactionRepository,
                           OrderRepository orderRepository,
                           PaymentTransactionMapper paymentTransactionMapper,
                           CacheManager cacheManager) {
        super(paymentTransactionRepository, cacheManager, PaymentTransaction.class.getName());
        this.gatewayConfigRepository = gatewayConfigRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.paymentTransactionMapper = paymentTransactionMapper;
    }

    @Transactional(readOnly = true)
    public PaymentConfigDto getConfig() {
        List<PaymentGatewayConfig> enabled = gatewayConfigRepository.findByEnabledTrue();
        String defaultGateway = gatewayConfigRepository.findByIsDefaultTrue()
                .map(c -> c.getGateway().name())
                .orElse(enabled.isEmpty() ? null : enabled.get(0).getGateway().name());

        return PaymentConfigDto.builder()
                .publishableKey(stripePublishableKey)
                .defaultGateway(defaultGateway)
                .availableGateways(enabled.stream().map(c -> c.getGateway().name()).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodDto> getMethods() {
        return gatewayConfigRepository.findByEnabledTrue().stream()
                .flatMap(config -> config.getMethods().stream()
                        .map(method -> PaymentMethodDto.builder()
                                .gateway(config.getGateway().name())
                                .method(method)
                                .label(method)
                                .build()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GatewayConfigDto getGatewayConfig(String gateway) {
        PaymentGatewayConfig config = gatewayConfigRepository.findByGateway(parseGateway(gateway))
                .orElseThrow(() -> new ResourceNotFoundException("Gateway de paiement introuvable: " + gateway));

        return GatewayConfigDto.builder()
                .gateway(config.getGateway().name())
                .config(config.getConfig())
                .methods(config.getMethods())
                .build();
    }

    /**
     * {@code orderId} doit appartenir à {@code userId} — avant la migration vers
     * {@code AbstractController}, ce contrôle n'existait pas du tout (n'importe
     * quel utilisateur authentifié pouvait créer une intention de paiement pour
     * la commande de n'importe qui). Corrigé ici en réutilisant
     * {@code OrderRepository.findByIdAndUserId}, déjà présent pour
     * {@code OrderService}.
     */
    public PaymentIntentDto createIntent(UUID userId, UUID orderId, String gateway, String phoneNumber) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable: " + orderId));

        PaymentGateway resolvedGateway = gateway != null ? parseGateway(gateway) : resolveDefaultGateway();

        if (isMobileMoney(resolvedGateway) && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new BadRequestException("Le numéro de téléphone est requis pour le Mobile Money");
        }

        // TODO: appeler le SDK Stripe (PaymentIntent.create) ou l'API du gateway Mobile Money choisi.
        PaymentTransaction transaction = PaymentTransaction.builder()
                .order(order)
                .gateway(resolvedGateway)
                .providerPaymentId(UUID.randomUUID().toString())
                .clientSecret(UUID.randomUUID().toString())
                .amount(order.getTotal())
                .currency("XAF")
                .status(PaymentStatus.PENDING)
                .phoneNumber(phoneNumber)
                .build();
        transaction = paymentTransactionRepository.save(transaction);
        clearCache();

        return PaymentIntentDto.builder()
                .id(transaction.getProviderPaymentId())
                .clientSecret(transaction.getClientSecret())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();
    }

    /** Même correctif d'ownership que {@link #createIntent} : la transaction doit appartenir à {@code userId}. */
    public PaymentConfirmResultDto confirm(UUID userId, String paymentIntentId, String gateway) {
        PaymentTransaction transaction = paymentTransactionRepository.findByProviderPaymentId(paymentIntentId)
                .filter(tx -> tx.getOrder().getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction de paiement introuvable"));

        // TODO: vérifier le statut réel auprès du gateway avant de marquer le paiement comme réussi.
        transaction.setStatus(PaymentStatus.SUCCEEDED);
        paymentTransactionRepository.save(transaction);

        Order order = transaction.getOrder();
        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        orderRepository.save(order);
        clearCache();

        return PaymentConfirmResultDto.builder()
                .success(true)
                .transactionId(transaction.getId().toString())
                .status(transaction.getStatus().name())
                .message("Paiement confirmé")
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentTransactionDto getTransaction(UUID userId, UUID transactionId) {
        return paymentTransactionMapper.toDto(findOwnedOrThrow(userId, transactionId));
    }

    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactions(UUID userId, Pageable pageable) {
        return paymentTransactionRepository.findByOrder_UserId(userId, pageable).map(paymentTransactionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countTransactions(UUID userId) {
        return paymentTransactionRepository.countByOrder_UserId(userId);
    }

    private PaymentTransaction findOwnedOrThrow(UUID userId, UUID transactionId) {
        return paymentTransactionRepository.findByIdAndOrder_UserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction de paiement introuvable: " + transactionId));
    }

    private PaymentGateway resolveDefaultGateway() {
        return gatewayConfigRepository.findByIsDefaultTrue()
                .map(PaymentGatewayConfig::getGateway)
                .orElseThrow(() -> new BadRequestException("Aucun gateway de paiement par défaut configuré"));
    }

    private boolean isMobileMoney(PaymentGateway gateway) {
        return gateway == PaymentGateway.MTN_MOBILE_MONEY || gateway == PaymentGateway.ORANGE_MONEY;
    }

    private PaymentGateway parseGateway(String gateway) {
        try {
            return PaymentGateway.valueOf(gateway.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Gateway de paiement inconnu: " + gateway);
        }
    }

    @Override
    protected PaymentTransactionDto convertToDTO(PaymentTransaction model) {
        return paymentTransactionMapper.toDto(model);
    }

    @Override
    protected PaymentTransaction convertToModel(PaymentTransactionDto dto) {
        return paymentTransactionMapper.toModel(dto);
    }

    /** Jamais appelée : {@code CREATE} est exclu d'{@code allowedOperations()} — voir {@link #createIntent}. */
    @Override
    public PaymentTransactionDto create(PaymentTransactionRequest dto) {
        throw new UnsupportedOperationException(
                "Utiliser PaymentService.createIntent(userId, orderId, gateway, phoneNumber).");
    }

    /** Jamais appelée : une transaction ne se met à jour que via {@link #confirm} ou le webhook gateway. */
    @Override
    public PaymentTransactionDto update(PaymentTransactionRequest dto, UUID id) {
        throw new UnsupportedOperationException(
                "Une transaction de paiement ne se met pas à jour génériquement : voir confirm() / PaymentWebhookController.");
    }

    @Override
    protected Class<PaymentTransaction> getEntityClass() {
        return PaymentTransaction.class;
    }
}

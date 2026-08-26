package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.OrderDto;
import com.tondise.ecommerce.dao.enums.OrderStatus;
import com.tondise.ecommerce.dao.enums.PaymentStatus;
import com.tondise.ecommerce.dao.enums.PromoType;
import com.tondise.ecommerce.dao.mappers.OrderMapper;
import com.tondise.ecommerce.dao.models.Address;
import com.tondise.ecommerce.dao.models.Cart;
import com.tondise.ecommerce.dao.models.CartItem;
import com.tondise.ecommerce.dao.models.Order;
import com.tondise.ecommerce.dao.models.OrderItem;
import com.tondise.ecommerce.dao.models.PromoCode;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.repository.AddressRepository;
import com.tondise.ecommerce.dao.repository.CartRepository;
import com.tondise.ecommerce.dao.repository.OrderRepository;
import com.tondise.ecommerce.dao.repository.UserRepository;
import com.tondise.ecommerce.dao.request.CreateOrderRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService extends AbstractService<Order, OrderDto, CreateOrderRequest> {

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.PROCESSING);
    private static final BigDecimal DEFAULT_SHIPPING_COST = BigDecimal.valueOf(2000);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                         AddressRepository addressRepository, UserRepository userRepository,
                         OrderMapper orderMapper, CacheManager cacheManager) {
        super(orderRepository, cacheManager, Order.class.getName());
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
    }

    public OrderDto createOrder(UUID userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Panier introuvable"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Le panier est vide");
        }

        Address shippingAddress = addressRepository.findByIdAndUserId(request.getShippingAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse de livraison introuvable"));

        Address billingAddress = request.getBillingAddressId() != null
                ? addressRepository.findByIdAndUserId(request.getBillingAddressId(), userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Adresse de facturation introuvable"))
                : shippingAddress;

        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        String promoCodeLabel = null;
        if (cart.getPromoCode() != null) {
            PromoCode promo = cart.getPromoCode();
            promoCodeLabel = promo.getCode();
            discount = promo.getType() == PromoType.PERCENTAGE
                    ? subtotal.multiply(promo.getDiscount()).divide(BigDecimal.valueOf(100))
                    : promo.getDiscount();
        }

        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingMethod(request.getShippingMethod())
                .shippingCost(DEFAULT_SHIPPING_COST)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .subtotal(subtotal)
                .discount(discount)
                .total(subtotal.subtract(discount).add(DEFAULT_SHIPPING_COST))
                .promoCode(promoCodeLabel)
                .notes(request.getNotes())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .productName(cartItem.getProduct().getName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(cartItem.getTotalPrice())
                    .build();
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);

        cart.getItems().clear();
        cart.setPromoCode(null);
        cartRepository.save(cart);

        clearCache();
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID userId, UUID orderId) {
        return orderMapper.toDto(findOwnedOrThrow(userId, orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countOrders(UUID userId) {
        return orderRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toDto);
    }

    public OrderDto updateStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable: " + orderId));
        order.setStatus(status);
        OrderDto updated = orderMapper.toDto(orderRepository.save(order));
        clearCache();
        return updated;
    }

    public OrderDto cancelOrder(UUID userId, UUID orderId) {
        Order order = findOwnedOrThrow(userId, orderId);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new BadRequestException("Cette commande ne peut plus être annulée");
        }

        order.setStatus(OrderStatus.CANCELLED);
        OrderDto cancelled = orderMapper.toDto(orderRepository.save(order));
        clearCache();
        return cancelled;
    }

    public OrderDto reorder(UUID userId, UUID orderId) {
        Order source = findOwnedOrThrow(userId, orderId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier introuvable"));

        source.getItems().forEach(sourceItem -> cart.getItems().add(CartItem.builder()
                .cart(cart)
                .product(sourceItem.getProduct())
                .quantity(sourceItem.getQuantity())
                .unitPrice(sourceItem.getUnitPrice())
                .totalPrice(sourceItem.getTotalPrice())
                .build()));

        cartRepository.save(cart);
        return orderMapper.toDto(source);
    }

    private Order findOwnedOrThrow(UUID userId, UUID orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable: " + orderId));
    }

    private String generateOrderNumber() {
        return "ORD-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    protected OrderDto convertToDTO(Order model) {
        return orderMapper.toDto(model);
    }

    @Override
    protected Order convertToModel(OrderDto dto) {
        return orderMapper.toModel(dto);
    }

    /**
     * Jamais appelée : {@code OrderController.create} est surchargé pour passer
     * par {@link #createOrder(UUID, CreateOrderRequest)}, seul point d'entrée qui
     * a le contexte utilisateur (panier, adresses possédées) nécessaire pour
     * construire une commande.
     */
    @Override
    public OrderDto create(CreateOrderRequest dto) {
        throw new UnsupportedOperationException(
                "Utiliser OrderService.createOrder(userId, request) : une commande se construit à partir du panier de l'utilisateur.");
    }

    /**
     * Jamais appelée : {@code OrderController} exclut {@code UPDATE} de ses
     * {@code allowedOperations()} — une commande ne change d'état que via
     * {@link #cancelOrder}, {@link #reorder} ou {@link #updateStatus} (admin),
     * jamais par écrasement générique de ses champs.
     */
    @Override
    public OrderDto update(CreateOrderRequest dto, UUID id) {
        throw new UnsupportedOperationException(
                "Une commande ne se met pas à jour génériquement : voir cancelOrder/reorder/updateStatus.");
    }

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }
}

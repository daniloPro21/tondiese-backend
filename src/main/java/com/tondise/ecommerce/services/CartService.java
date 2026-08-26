package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.CartDto;
import com.tondise.ecommerce.dao.dto.PriceCalculationDto;
import com.tondise.ecommerce.dao.enums.PromoType;
import com.tondise.ecommerce.dao.mappers.CartMapper;
import com.tondise.ecommerce.dao.models.Cart;
import com.tondise.ecommerce.dao.models.CartItem;
import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.models.PromoCode;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.repository.CartItemRepository;
import com.tondise.ecommerce.dao.repository.CartRepository;
import com.tondise.ecommerce.dao.repository.ProductRepository;
import com.tondise.ecommerce.dao.repository.PromoCodeRepository;
import com.tondise.ecommerce.dao.repository.UserRepository;
import com.tondise.ecommerce.dao.request.AddToCartRequest;
import com.tondise.ecommerce.dao.request.CartRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService extends AbstractService<Cart, CartDto, CartRequest> {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                        ProductRepository productRepository, PromoCodeRepository promoCodeRepository,
                        UserRepository userRepository, ProductService productService, CartMapper cartMapper,
                        CacheManager cacheManager) {
        super(cartRepository, cacheManager, Cart.class.getName());
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.cartMapper = cartMapper;
    }

    @Transactional(readOnly = true)
    public CartDto getCart(UUID userId) {
        return toDto(findOrCreateCart(userId));
    }

    public CartDto addItem(UUID userId, AddToCartRequest request) {
        Cart cart = findOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable: " + request.getProductId()));

        Map<UUID, UUID> selectedOptions = request.getSelectedOptions() == null ? Map.of() : request.getSelectedOptions();

        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())
                        && item.getSelectedOptions().equals(selectedOptions))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing.setTotalPrice(existing.getUnitPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
            cartItemRepository.save(existing);
            return toDto(cart);
        }

        PriceCalculationDto price = productService.calculatePrice(product.getId(), request.getQuantity(), selectedOptions);

        CartItem item = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .selectedOptions(selectedOptions)
                .designId(request.getDesignId())
                .unitPrice(price.getUnitPrice())
                .totalPrice(price.getSubtotal())
                .build();
        cart.getItems().add(item);
        cartItemRepository.save(item);

        return toDto(cart);
    }

    public CartDto updateItem(UUID userId, UUID itemId, int quantity) {
        Cart cart = findOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Article du panier introuvable: " + itemId));

        item.setQuantity(quantity);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(item);

        return toDto(cart);
    }

    public void removeItem(UUID userId, UUID itemId) {
        Cart cart = findOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Article du panier introuvable: " + itemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }

    public CartDto applyPromoCode(UUID userId, String code) {
        Cart cart = findOrCreateCart(userId);
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .orElseThrow(() -> new BadRequestException("Code promo invalide ou expiré"));

        if (promoCode.getExpiresAt() != null && promoCode.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Ce code promo a expiré");
        }

        cart.setPromoCode(promoCode);
        cartRepository.save(cart);
        return toDto(cart);
    }

    public CartDto removePromoCode(UUID userId) {
        Cart cart = findOrCreateCart(userId);
        cart.setPromoCode(null);
        cartRepository.save(cart);
        return toDto(cart);
    }

    /** Vide le panier (articles + code promo), sans le supprimer — voir {@code CartController.deleteById}. */
    public CartDto clearCart(UUID userId) {
        Cart cart = findOrCreateCart(userId);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setPromoCode(null);
        cartRepository.save(cart);
        return toDto(cart);
    }

    private Cart findOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private CartDto toDto(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        String promoCode = null;
        if (cart.getPromoCode() != null) {
            PromoCode promo = cart.getPromoCode();
            promoCode = promo.getCode();
            discount = promo.getType() == PromoType.PERCENTAGE
                    ? subtotal.multiply(promo.getDiscount()).divide(BigDecimal.valueOf(100))
                    : promo.getDiscount();
        }

        return CartDto.builder()
                .id(cart.getId())
                .deleted(cart.isDeleted())
                .deletedOn(cart.getDeletedOn())
                .created(cart.getCreated())
                .updated(cart.getUpdated())
                .items(cartMapper.toItemDtoList(cart.getItems()))
                .itemsCount(cart.getItems().size())
                .subtotal(subtotal)
                .discount(discount)
                .promoCode(promoCode)
                .total(subtotal.subtract(discount))
                .build();
    }

    @Override
    protected CartDto convertToDTO(Cart model) {
        return toDto(model);
    }

    @Override
    protected Cart convertToModel(CartDto dto) {
        return cartMapper.toModel(dto);
    }

    /** Jamais appelée : {@code CartController.create} passe par {@link #getCart(UUID)} (get-or-create). */
    @Override
    public CartDto create(CartRequest dto) {
        throw new UnsupportedOperationException(
                "Un panier n'est jamais créé avec des champs client : voir CartService.getCart(userId).");
    }

    /** Un panier ne se met pas à jour globalement : voir addItem/updateItem/removeItem/applyPromoCode. */
    @Override
    public CartDto update(CartRequest dto, UUID id) {
        throw new UnsupportedOperationException(
                "Un panier ne se met pas à jour génériquement : gérer ses articles individuellement.");
    }

    @Override
    protected Class<Cart> getEntityClass() {
        return Cart.class;
    }
}

package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.FavoriteDto;
import com.tondise.ecommerce.dao.dto.ProductSummaryDto;
import com.tondise.ecommerce.dao.mappers.FavoriteMapper;
import com.tondise.ecommerce.dao.mappers.ProductMapper;
import com.tondise.ecommerce.dao.models.Favorite;
import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.repository.FavoriteRepository;
import com.tondise.ecommerce.dao.repository.ProductRepository;
import com.tondise.ecommerce.dao.repository.UserRepository;
import com.tondise.ecommerce.dao.request.FavoriteRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteService extends AbstractService<Favorite, FavoriteDto, FavoriteRequest> {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final FavoriteMapper favoriteMapper;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository,
                            UserRepository userRepository, ProductMapper productMapper,
                            FavoriteMapper favoriteMapper, CacheManager cacheManager) {
        super(favoriteRepository, cacheManager, Favorite.class.getName());
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productMapper = productMapper;
        this.favoriteMapper = favoriteMapper;
    }

    /** Forme historique de l'endpoint (avant migration vers {@link FavoriteDto}) : les produits favoris, sans enveloppe. */
    @Transactional(readOnly = true)
    public List<ProductSummaryDto> getFavoriteProducts(UUID userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getProduct)
                .map(productMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> getFavorites(UUID userId) {
        return favoriteMapper.toDtoList(favoriteRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public Page<FavoriteDto> getFavorites(UUID userId, Pageable pageable) {
        return favoriteRepository.findByUserId(userId, pageable).map(favoriteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countFavorites(UUID userId) {
        return favoriteRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public FavoriteDto getFavorite(UUID userId, UUID favoriteId) {
        return favoriteMapper.toDto(findOwnedOrThrow(userId, favoriteId));
    }

    public FavoriteDto addFavorite(UUID userId, UUID productId) {
        Favorite existing = favoriteRepository.findByUserIdAndProductId(userId, productId).orElse(null);
        if (existing != null) {
            return favoriteMapper.toDto(existing);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable: " + productId));

        FavoriteDto saved = favoriteMapper.toDto(
                favoriteRepository.save(Favorite.builder().user(user).product(product).build()));
        clearCache();
        return saved;
    }

    public void removeFavorite(UUID userId, UUID productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        clearCache();
    }

    public void removeFavoriteById(UUID userId, UUID favoriteId) {
        favoriteRepository.delete(findOwnedOrThrow(userId, favoriteId));
        clearCache();
    }

    private Favorite findOwnedOrThrow(UUID userId, UUID favoriteId) {
        return favoriteRepository.findByIdAndUserId(favoriteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Favori introuvable: " + favoriteId));
    }

    @Override
    protected FavoriteDto convertToDTO(Favorite model) {
        return favoriteMapper.toDto(model);
    }

    @Override
    protected Favorite convertToModel(FavoriteDto dto) {
        return favoriteMapper.toModel(dto);
    }

    /** Jamais appelée : {@code FavoriteController.create} passe par {@link #addFavorite(UUID, UUID)}. */
    @Override
    public FavoriteDto create(FavoriteRequest dto) {
        throw new UnsupportedOperationException(
                "Utiliser FavoriteService.addFavorite(userId, productId) : un favori appartient toujours à un utilisateur.");
    }

    /** Un favori ne se met pas à jour : on l'ajoute ou on le retire. */
    @Override
    public FavoriteDto update(FavoriteRequest dto, UUID id) {
        throw new UnsupportedOperationException("Un favori ne se met pas à jour : voir addFavorite/removeFavorite.");
    }

    @Override
    protected Class<Favorite> getEntityClass() {
        return Favorite.class;
    }
}

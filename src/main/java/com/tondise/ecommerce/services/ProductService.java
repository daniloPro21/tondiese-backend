package com.tondise.ecommerce.services;

import com.tondise.ecommerce.dao.dto.PriceCalculationDto;
import com.tondise.ecommerce.dao.dto.ProductDto;
import com.tondise.ecommerce.dao.dto.ProductSummaryDto;
import com.tondise.ecommerce.dao.mappers.ProductMapper;
import com.tondise.ecommerce.dao.models.Category;
import com.tondise.ecommerce.dao.models.PricingTier;
import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.models.ProductOptionValue;
import com.tondise.ecommerce.dao.repository.CategoryRepository;
import com.tondise.ecommerce.dao.repository.ProductOptionValueRepository;
import com.tondise.ecommerce.dao.repository.ProductRepository;
import com.tondise.ecommerce.dao.request.ProductFilterRequest;
import com.tondise.ecommerce.dao.request.ProductRequest;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService extends AbstractService<Product, ProductDto, ProductRequest> {

    private final ProductRepository productRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                           ProductOptionValueRepository productOptionValueRepository,
                           CategoryRepository categoryRepository,
                           ProductMapper productMapper,
                           CacheManager cacheManager) {
        super(productRepository, cacheManager, Product.class.getName());
        this.productRepository = productRepository;
        this.productOptionValueRepository = productOptionValueRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    public Page<ProductSummaryDto> getProducts(ProductFilterRequest filters) {
        Pageable pageable = toPageable(filters);
        Page<Product> page = filters.getCategoryId() != null
                ? productRepository.findByCategoryId(filters.getCategoryId(), pageable)
                : productRepository.findAll(pageable);
        return page.map(productMapper::toSummaryDto);
    }

    public Page<ProductSummaryDto> getProductsByCategory(UUID categoryId, ProductFilterRequest filters) {
        return productRepository.findByCategoryId(categoryId, toPageable(filters))
                .map(productMapper::toSummaryDto);
    }

    public List<ProductSummaryDto> getFeaturedProducts() {
        return productMapper.toSummaryDtoList(
                productRepository.findByFeaturedTrue(PageRequest.of(0, 12)).getContent());
    }

    public Page<ProductSummaryDto> search(String query, ProductFilterRequest filters) {
        return productRepository.search(query, toPageable(filters)).map(productMapper::toSummaryDto);
    }

    public PriceCalculationDto calculatePrice(UUID productId, int quantity, Map<UUID, UUID> options) {
        Product product = findProductOrThrow(productId);

        BigDecimal unitPrice = product.getPricingTiers().stream()
                .filter(tier -> quantity >= tier.getQuantity())
                .max(Comparator.comparingInt(PricingTier::getQuantity))
                .map(PricingTier::getUnitPrice)
                .orElse(product.getBasePrice());

        BigDecimal optionsPrice = BigDecimal.ZERO;
        if (options != null) {
            for (UUID valueId : options.values()) {
                ProductOptionValue value = productOptionValueRepository.findById(valueId)
                        .orElseThrow(() -> new ResourceNotFoundException("Option de produit introuvable: " + valueId));
                optionsPrice = optionsPrice.add(value.getPriceModifier());
            }
        }

        BigDecimal effectiveUnitPrice = unitPrice.add(optionsPrice);
        BigDecimal subtotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(quantity));

        return PriceCalculationDto.builder()
                .unitPrice(effectiveUnitPrice)
                .quantity(quantity)
                .optionsPrice(optionsPrice)
                .subtotal(subtotal)
                .breakdown(Map.of(
                        "base_price", unitPrice,
                        "options", optionsPrice,
                        "quantity", BigDecimal.valueOf(quantity)))
                .build();
    }

    @Override
    @Transactional
    public ProductDto create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .mainImage(request.getMainImage())
                .images(request.getImages() != null ? request.getImages() : List.of())
                .category(request.getCategoryId() != null ? findCategoryOrThrow(request.getCategoryId()) : null)
                .featured(request.isFeatured())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .build();
        ProductDto saved = productMapper.toDto(productRepository.save(product));
        clearCache();
        return saved;
    }

    @Override
    @Transactional
    public ProductDto update(ProductRequest request, UUID id) {
        Product product = findProductOrThrow(id);
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages() != null ? request.getImages() : List.of());
        product.setCategory(request.getCategoryId() != null ? findCategoryOrThrow(request.getCategoryId()) : null);
        product.setFeatured(request.isFeatured());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        ProductDto updated = productMapper.toDto(productRepository.save(product));
        clearCache();
        return updated;
    }

    @Override
    protected ProductDto convertToDTO(Product model) {
        return productMapper.toDto(model);
    }

    @Override
    protected Product convertToModel(ProductDto dto) {
        return productMapper.toModel(dto);
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable: " + categoryId));
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable: " + id));
    }

    private Pageable toPageable(ProductFilterRequest filters) {
        Sort sort = switch (filters.getSort() == null ? "" : filters.getSort()) {
            case "price_asc" -> Sort.by("basePrice").ascending();
            case "price_desc" -> Sort.by("basePrice").descending();
            case "name" -> Sort.by("name").ascending();
            default -> Sort.by("created").descending();
        };
        return PageRequest.of(filters.getPage(), filters.getPerPage(), sort);
    }
}

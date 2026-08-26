package com.tondise.ecommerce.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tondise.ecommerce.dao.dto.PriceCalculationDto;
import com.tondise.ecommerce.dao.models.PricingTier;
import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.repository.CategoryRepository;
import com.tondise.ecommerce.dao.repository.ProductOptionValueRepository;
import com.tondise.ecommerce.dao.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionValueRepository productOptionValueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void calculatePrice_usesBestMatchingPricingTier() {
        ProductService productService = new ProductService(
                productRepository, productOptionValueRepository, categoryRepository, null, null);

        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .basePrice(BigDecimal.valueOf(1000))
                .pricingTiers(List.of(
                        PricingTier.builder().quantity(1).unitPrice(BigDecimal.valueOf(1000)).build(),
                        PricingTier.builder().quantity(10).unitPrice(BigDecimal.valueOf(800)).build()))
                .build();

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        PriceCalculationDto result = productService.calculatePrice(productId, 12, Map.of());

        assertThat(result.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(9600));
    }
}

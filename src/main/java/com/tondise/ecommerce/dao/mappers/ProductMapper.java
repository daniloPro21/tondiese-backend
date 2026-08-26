package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.ProductDto;
import com.tondise.ecommerce.dao.dto.ProductOptionDto;
import com.tondise.ecommerce.dao.dto.ProductOptionValueDto;
import com.tondise.ecommerce.dao.dto.ProductSummaryDto;
import com.tondise.ecommerce.dao.dto.PricingTierDto;
import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.models.ProductOption;
import com.tondise.ecommerce.dao.models.ProductOptionValue;
import com.tondise.ecommerce.dao.models.PricingTier;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = CategoryMapper.class)
public interface ProductMapper extends BaseMapper<Product, ProductDto> {

    @Mapping(target = "isFeatured", expression = "java(product.isFeatured())")
    ProductDto toDto(Product product);

    @Mapping(target = "options", ignore = true)
    @Mapping(target = "pricingTiers", ignore = true)
    Product toModel(ProductDto productDto);

    @Mapping(target = "isFeatured", expression = "java(product.isFeatured())")
    ProductSummaryDto toSummaryDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    List<ProductSummaryDto> toSummaryDtoList(List<Product> products);

    @Mapping(target = "isRequired", expression = "java(option.isRequired())")
    ProductOptionDto toOptionDto(ProductOption option);

    @Mapping(target = "isDefault", expression = "java(value.isDefault())")
    ProductOptionValueDto toOptionValueDto(ProductOptionValue value);

    PricingTierDto toPricingTierDto(PricingTier tier);
}

package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.CategoryDto;
import com.tondise.ecommerce.dao.models.Category;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface CategoryMapper extends BaseMapper<Category, CategoryDto> {

    CategoryDto toDto(Category category);

    @Mapping(target = "parent", ignore = true)
    Category toModel(CategoryDto categoryDto);

    List<CategoryDto> toDtoList(List<Category> categories);
}

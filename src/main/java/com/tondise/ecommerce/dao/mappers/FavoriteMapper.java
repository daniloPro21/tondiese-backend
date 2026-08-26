package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.FavoriteDto;
import com.tondise.ecommerce.dao.models.Favorite;
import com.tondise.ecommerce.dao.request.FavoriteRequest;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = ProductMapper.class)
public interface FavoriteMapper extends BaseMapper<Favorite, FavoriteDto> {

    @Mapping(target = "productId", source = "product.id")
    FavoriteDto toDto(Favorite favorite);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Favorite toModel(FavoriteDto dto);

    /** {@code user} et {@code product} sont résolus par repository dans le service, pas mappables ici. */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Favorite toModelRequest(FavoriteRequest request);

    List<FavoriteDto> toDtoList(List<Favorite> favorites);
}

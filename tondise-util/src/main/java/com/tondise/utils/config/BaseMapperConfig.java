package com.tondise.utils.config;

import com.tondise.utils.abstractModel.AbstractDTO;
import com.tondise.utils.abstractModel.AbstractEntity;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

@MapperConfig(
        componentModel = "spring",
        uses = { DateTimeMapper.class }
)
public interface BaseMapperConfig {

    @Mapping(source = "created", target = "created")
    @Mapping(source = "updated", target = "updated")
    AbstractDTO toDto(AbstractEntity entity);
}

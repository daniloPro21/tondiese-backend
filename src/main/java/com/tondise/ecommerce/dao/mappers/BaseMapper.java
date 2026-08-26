package com.tondise.ecommerce.dao.mappers;


import com.tondise.utils.abstractModel.AbstractDTO;
import com.tondise.utils.abstractModel.AbstractEntity;

public interface BaseMapper<E extends AbstractEntity, D extends AbstractDTO> {

    D toDto(E entity);

    E toModel(D dto);
}
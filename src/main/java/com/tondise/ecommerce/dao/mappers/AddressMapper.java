package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.AddressDto;
import com.tondise.ecommerce.dao.models.Address;
import com.tondise.ecommerce.dao.request.AddressRequest;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface AddressMapper extends BaseMapper<Address, AddressDto> {

    @Mapping(target = "isDefault", expression = "java(address.isDefault())")
    AddressDto toDto(Address address);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", expression = "java(addressDto.isDefault())")
    Address toModel(AddressDto addressDto);

    /**
     * Le propriétaire ({@code user}) n'est jamais pris depuis le payload client : voir {@code AddressService}.
     * {@code isDefault} est mappé explicitement : MapStruct ne relie pas automatiquement un accesseur
     * booléen {@code isXxx()} des deux côtés (ambiguïté connue Lombok/JavaBean sur le préfixe {@code is}) —
     * sans ce {@code @Mapping}, le champ était silencieusement ignoré (toujours {@code false}).
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", expression = "java(request.isDefault())")
    Address toModelRequest(AddressRequest request);

    List<AddressDto> toDtoList(List<Address> addresses);
}

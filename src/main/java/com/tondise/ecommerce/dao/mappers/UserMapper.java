package com.tondise.ecommerce.dao.mappers;

import com.tondise.ecommerce.dao.dto.UserDto;
import com.tondise.ecommerce.dao.models.User;
import com.tondise.ecommerce.dao.request.UpdateProfileRequest;
import com.tondise.utils.config.BaseMapper;
import com.tondise.utils.config.BaseMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface UserMapper extends BaseMapper<User, UserDto> {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDto toDto(User user);

    @Mapping(target = "keycloakUserId", ignore = true)
    User toModel(UserDto dto);

    /** {@code email}/{@code keycloakUserId} ne viennent jamais d'un payload de mise à jour de profil. */
    @Mapping(target = "keycloakUserId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "emailVerifiedAt", ignore = true)
    User toModelRequest(UpdateProfileRequest request);
}

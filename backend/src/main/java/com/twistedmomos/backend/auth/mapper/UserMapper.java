package com.twistedmomos.backend.auth.mapper;

import com.twistedmomos.backend.auth.dto.response.UserResponse;
import com.twistedmomos.backend.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role.name")
    UserResponse toResponse(User user);
}

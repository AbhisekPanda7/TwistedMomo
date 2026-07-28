package com.twistedmomos.backend.mapper;

import com.twistedmomos.backend.dto.response.UserResponse;
import com.twistedmomos.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role.name")
    UserResponse toResponse(User user);
}

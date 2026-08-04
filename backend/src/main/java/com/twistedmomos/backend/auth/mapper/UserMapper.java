package com.twistedmomos.backend.auth.mapper;

import com.twistedmomos.backend.auth.dto.response.UserResponse;
import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.User;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.primaryRole().name())")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleNames")
    UserResponse toResponse(User user);

    @Named("roleNames")
    static List<String> roleNames(Set<Role> roles) {
        return roles.stream().map(r -> r.getName().name()).sorted().toList();
    }
}

package com.twistedmomos.backend.auth.mapper;

import com.twistedmomos.backend.auth.dto.response.UserAdminResponse;
import com.twistedmomos.backend.auth.entity.Role;
import com.twistedmomos.backend.auth.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserAdminMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleNames")
    UserAdminResponse toResponse(User user);

    @Named("roleNames")
    static Set<String> roleNames(Set<Role> roles) {
        return roles.stream().map(r -> r.getName().name()).collect(Collectors.toSet());
    }
}

package com.twistedmomos.backend.mapper;

import com.twistedmomos.backend.dto.response.MenuItemResponse;
import com.twistedmomos.backend.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    MenuItemResponse toResponse(MenuItem menuItem);
}

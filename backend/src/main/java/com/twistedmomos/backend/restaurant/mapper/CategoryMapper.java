package com.twistedmomos.backend.restaurant.mapper;

import com.twistedmomos.backend.restaurant.dto.response.CategoryResponse;
import com.twistedmomos.backend.restaurant.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}

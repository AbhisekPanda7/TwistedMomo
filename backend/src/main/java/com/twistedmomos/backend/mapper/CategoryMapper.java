package com.twistedmomos.backend.mapper;

import com.twistedmomos.backend.dto.response.CategoryResponse;
import com.twistedmomos.backend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}

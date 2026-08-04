package com.twistedmomos.backend.restaurant.service;

import com.twistedmomos.backend.restaurant.dto.request.CategoryRequest;
import com.twistedmomos.backend.restaurant.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

    List<CategoryResponse> listPublic();

    List<CategoryResponse> listAdmin();

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}

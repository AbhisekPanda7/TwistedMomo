package com.twistedmomos.backend.service.impl;

import com.twistedmomos.backend.dto.request.CategoryRequest;
import com.twistedmomos.backend.dto.response.CategoryResponse;
import com.twistedmomos.backend.entity.Category;
import com.twistedmomos.backend.exception.DuplicateResourceException;
import com.twistedmomos.backend.exception.ResourceInUseException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.mapper.CategoryMapper;
import com.twistedmomos.backend.repository.CategoryRepository;
import com.twistedmomos.backend.repository.MenuItemRepository;
import com.twistedmomos.backend.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> listPublic() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> listAdmin() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("A category with slug '" + request.slug() + "' already exists");
        }
        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() == null || request.active())
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);
        if (categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new DuplicateResourceException("A category with slug '" + request.slug() + "' already exists");
        }
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }
        if (request.active() != null) {
            category.setActive(request.active());
        }
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        long itemCount = menuItemRepository.countByCategoryId(id);
        if (itemCount > 0) {
            throw new ResourceInUseException(
                    "Cannot delete category: it still has " + itemCount + " menu item(s) assigned. Reassign or delete them first.");
        }
        categoryRepository.delete(category);
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category " + id + " not found"));
    }
}

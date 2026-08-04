package com.twistedmomos.backend.restaurant.service.impl;

import com.twistedmomos.backend.restaurant.dto.request.CategoryRequest;
import com.twistedmomos.backend.restaurant.dto.response.CategoryResponse;
import com.twistedmomos.backend.restaurant.entity.Category;
import com.twistedmomos.backend.exception.DuplicateResourceException;
import com.twistedmomos.backend.restaurant.exception.ResourceInUseException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.restaurant.mapper.CategoryMapper;
import com.twistedmomos.backend.restaurant.repository.CategoryRepository;
import com.twistedmomos.backend.restaurant.repository.MenuItemRepository;
import com.twistedmomos.backend.restaurant.service.CategoryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info("Creating category: slug={}", request.slug());

        if (categoryRepository.existsBySlug(request.slug())) {
            log.warn("Category create rejected — slug already exists: slug={}", request.slug());
            throw new DuplicateResourceException("A category with slug '" + request.slug() + "' already exists");
        }
        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(request.active() == null || request.active())
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Category created: categoryId={} slug={} active={} displayOrder={}",
                saved.getId(), saved.getSlug(), saved.isActive(), saved.getDisplayOrder());
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Updating category: categoryId={}", id);

        Category category = findOrThrow(id);
        if (categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
            log.warn("Category update rejected — slug taken by another category: categoryId={} slug={}",
                    id, request.slug());
            throw new DuplicateResourceException("A category with slug '" + request.slug() + "' already exists");
        }

        // Captured before mutation so the log names what actually changed rather than
        // what the request happened to carry.
        List<String> changed = new ArrayList<>();
        if (!Objects.equals(category.getName(), request.name())) {
            changed.add("name");
        }
        if (!Objects.equals(category.getSlug(), request.slug())) {
            changed.add("slug");
        }
        if (!Objects.equals(category.getDescription(), request.description())) {
            changed.add("description");
        }
        if (request.displayOrder() != null && !Objects.equals(category.getDisplayOrder(), request.displayOrder())) {
            changed.add("displayOrder");
        }
        if (request.active() != null && category.isActive() != request.active()) {
            changed.add("active");
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
        Category saved = categoryRepository.save(category);
        log.info("Category updated: categoryId={} fieldsChanged={}", id, changed);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting category: categoryId={}", id);

        Category category = findOrThrow(id);
        long itemCount = menuItemRepository.countByCategoryId(id);
        if (itemCount > 0) {
            log.warn("Category delete rejected — still in use: categoryId={} menuItems={}", id, itemCount);
            throw new ResourceInUseException(
                    "Cannot delete category: it still has " + itemCount + " menu item(s) assigned. Reassign or delete them first.");
        }
        categoryRepository.delete(category);
        log.info("Category deleted: categoryId={} slug={}", id, category.getSlug());
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found: categoryId={}", id);
                    return new ResourceNotFoundException("Category " + id + " not found");
                });
    }
}

package com.twistedmomos.backend.service.impl;

import com.twistedmomos.backend.dto.request.MenuItemRequest;
import com.twistedmomos.backend.dto.response.MenuItemResponse;
import com.twistedmomos.backend.dto.response.PageResponse;
import com.twistedmomos.backend.entity.Category;
import com.twistedmomos.backend.entity.MenuItem;
import com.twistedmomos.backend.entity.MenuItemTag;
import com.twistedmomos.backend.exception.DuplicateResourceException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.mapper.MenuItemMapper;
import com.twistedmomos.backend.repository.CategoryRepository;
import com.twistedmomos.backend.repository.MenuItemRepository;
import com.twistedmomos.backend.repository.specification.MenuItemSpecifications;
import com.twistedmomos.backend.service.FileStorageService;
import com.twistedmomos.backend.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuItemServiceImpl implements MenuItemService {

    private static final Sort DEFAULT_SORT = Sort.by("category.displayOrder", "displayOrder");

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemMapper menuItemMapper;
    private final FileStorageService fileStorageService;

    @Override
    public PageResponse<MenuItemResponse> search(Long categoryId, String q, Boolean veg, Integer maxSpicy, Pageable pageable) {
        Specification<MenuItem> spec = Specification
                .where(MenuItemSpecifications.fetchCategory())
                .and(MenuItemSpecifications.isAvailable(true))
                .and(MenuItemSpecifications.categoryId(categoryId))
                .and(MenuItemSpecifications.nameContains(q))
                .and(MenuItemSpecifications.isVeg(veg))
                .and(MenuItemSpecifications.maxSpicy(maxSpicy));
        Page<MenuItem> page = menuItemRepository.findAll(spec, withDefaultSort(pageable));
        return PageResponse.of(page.map(menuItemMapper::toResponse));
    }

    @Override
    public MenuItemResponse getPublicById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .filter(MenuItem::isAvailable)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item " + id + " not found"));
        return menuItemMapper.toResponse(menuItem);
    }

    @Override
    public PageResponse<MenuItemResponse> adminList(Long categoryId, Pageable pageable) {
        Specification<MenuItem> spec = Specification
                .where(MenuItemSpecifications.fetchCategory())
                .and(MenuItemSpecifications.categoryId(categoryId));
        Page<MenuItem> page = menuItemRepository.findAll(spec, withDefaultSort(pageable));
        return PageResponse.of(page.map(menuItemMapper::toResponse));
    }

    @Override
    public MenuItemResponse getAdminById(Long id) {
        return menuItemMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        if (menuItemRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("A menu item with slug '" + request.slug() + "' already exists");
        }
        Category category = findCategoryOrThrow(request.categoryId());

        MenuItem menuItem = MenuItem.builder()
                .category(category)
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .price(request.price())
                .veg(request.veg())
                .spicyLevel(request.spicyLevel())
                .tag(parseTag(request.tag()))
                .available(request.available() == null || request.available())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();

        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    @Override
    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem menuItem = findOrThrow(id);
        if (menuItemRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new DuplicateResourceException("A menu item with slug '" + request.slug() + "' already exists");
        }
        Category category = findCategoryOrThrow(request.categoryId());

        menuItem.setCategory(category);
        menuItem.setName(request.name());
        menuItem.setSlug(request.slug());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setVeg(request.veg());
        menuItem.setSpicyLevel(request.spicyLevel());
        menuItem.setTag(parseTag(request.tag()));
        if (request.available() != null) {
            menuItem.setAvailable(request.available());
        }
        if (request.displayOrder() != null) {
            menuItem.setDisplayOrder(request.displayOrder());
        }

        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        menuItemRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional
    public MenuItemResponse setAvailability(Long id, boolean available) {
        MenuItem menuItem = findOrThrow(id);
        menuItem.setAvailable(available);
        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    @Override
    @Transactional
    public MenuItemResponse uploadImage(Long id, MultipartFile file) {
        MenuItem menuItem = findOrThrow(id);
        String imageUrl = fileStorageService.store(file, "menu");
        menuItem.setImageUrl(imageUrl);
        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item " + id + " not found"));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category " + categoryId + " not found"));
    }

    private MenuItemTag parseTag(String tag) {
        return tag == null ? null : MenuItemTag.valueOf(tag);
    }

    private Pageable withDefaultSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
    }
}

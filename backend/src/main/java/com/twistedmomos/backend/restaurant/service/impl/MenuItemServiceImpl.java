package com.twistedmomos.backend.restaurant.service.impl;

import com.twistedmomos.backend.restaurant.dto.request.MenuItemRequest;
import com.twistedmomos.backend.restaurant.dto.response.MenuItemResponse;
import com.twistedmomos.backend.shared.dto.response.PageResponse;
import com.twistedmomos.backend.restaurant.entity.Category;
import com.twistedmomos.backend.restaurant.entity.MenuItem;
import com.twistedmomos.backend.restaurant.entity.MenuItemTag;
import com.twistedmomos.backend.exception.DuplicateResourceException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.restaurant.mapper.MenuItemMapper;
import com.twistedmomos.backend.restaurant.repository.CategoryRepository;
import com.twistedmomos.backend.restaurant.repository.MenuItemRepository;
import com.twistedmomos.backend.restaurant.repository.specification.MenuItemSpecifications;
import com.twistedmomos.backend.restaurant.service.FileStorageService;
import com.twistedmomos.backend.restaurant.service.MenuItemService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
        log.info("Creating menu item: slug={} categoryId={}", request.slug(), request.categoryId());

        if (menuItemRepository.existsBySlug(request.slug())) {
            log.warn("Menu item create rejected — slug already exists: slug={}", request.slug());
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

        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item created: menuItemId={} slug={} categoryId={} price={} available={}",
                saved.getId(), saved.getSlug(), category.getId(), saved.getPrice(), saved.isAvailable());
        return menuItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        log.info("Updating menu item: menuItemId={}", id);

        MenuItem menuItem = findOrThrow(id);
        if (menuItemRepository.existsBySlugAndIdNot(request.slug(), id)) {
            log.warn("Menu item update rejected — slug taken by another item: menuItemId={} slug={}",
                    id, request.slug());
            throw new DuplicateResourceException("A menu item with slug '" + request.slug() + "' already exists");
        }
        Category category = findCategoryOrThrow(request.categoryId());

        // Captured before mutation so the log names what actually changed rather than
        // what the request happened to carry. Price changes especially are worth an
        // audit trail — orders snapshot price at placement, so a change here is silent.
        List<String> changed = new ArrayList<>();
        if (!Objects.equals(menuItem.getCategory().getId(), category.getId())) {
            changed.add("category");
        }
        if (!Objects.equals(menuItem.getName(), request.name())) {
            changed.add("name");
        }
        if (!Objects.equals(menuItem.getSlug(), request.slug())) {
            changed.add("slug");
        }
        if (!Objects.equals(menuItem.getDescription(), request.description())) {
            changed.add("description");
        }
        if (menuItem.getPrice() == null || request.price() == null
                || menuItem.getPrice().compareTo(request.price()) != 0) {
            changed.add("price");
        }
        if (menuItem.isVeg() != request.veg()) {
            changed.add("veg");
        }
        if (!Objects.equals(menuItem.getSpicyLevel(), request.spicyLevel())) {
            changed.add("spicyLevel");
        }
        if (!Objects.equals(menuItem.getTag(), parseTag(request.tag()))) {
            changed.add("tag");
        }
        if (request.available() != null && menuItem.isAvailable() != request.available()) {
            changed.add("available");
        }
        if (request.displayOrder() != null && !Objects.equals(menuItem.getDisplayOrder(), request.displayOrder())) {
            changed.add("displayOrder");
        }

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

        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item updated: menuItemId={} fieldsChanged={}", id, changed);
        return menuItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting menu item: menuItemId={}", id);
        MenuItem menuItem = findOrThrow(id);
        menuItemRepository.delete(menuItem);
        // Placed orders keep their snapshotted name and price; OrderItem.menuItemId
        // simply becomes null. Deleting never rewrites order history.
        log.info("Menu item deleted: menuItemId={} slug={}", id, menuItem.getSlug());
    }

    @Override
    @Transactional
    public MenuItemResponse setAvailability(Long id, boolean available) {
        MenuItem menuItem = findOrThrow(id);
        boolean previous = menuItem.isAvailable();
        menuItem.setAvailable(available);
        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item availability changed: menuItemId={} from={} to={}", id, previous, available);
        return menuItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MenuItemResponse uploadImage(Long id, MultipartFile file) {
        log.info("Uploading menu item image: menuItemId={} sizeBytes={} contentType={}",
                id, file.getSize(), file.getContentType());

        MenuItem menuItem = findOrThrow(id);
        // store() validates type and size, and throws InvalidFileException on rejection.
        String imageUrl = fileStorageService.store(file, "menu");
        menuItem.setImageUrl(imageUrl);
        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item image uploaded: menuItemId={}", id);
        return menuItemMapper.toResponse(saved);
    }

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Menu item not found: menuItemId={}", id);
                    return new ResourceNotFoundException("Menu item " + id + " not found");
                });
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found for menu item: categoryId={}", categoryId);
                    return new ResourceNotFoundException("Category " + categoryId + " not found");
                });
    }

    private MenuItemTag parseTag(String tag) {
        if (tag == null) {
            return null;
        }
        try {
            return MenuItemTag.valueOf(tag);
        } catch (IllegalArgumentException ex) {
            // Reaches the catch-all handler as a 500 rather than a 400 — logged here
            // because the generic response tells the client nothing about the cause.
            log.warn("Unknown menu item tag requested: tag={}", tag);
            throw ex;
        }
    }

    private Pageable withDefaultSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
    }
}

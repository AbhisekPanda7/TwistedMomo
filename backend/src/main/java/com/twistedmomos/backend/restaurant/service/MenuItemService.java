package com.twistedmomos.backend.service;

import com.twistedmomos.backend.dto.request.MenuItemRequest;
import com.twistedmomos.backend.dto.response.MenuItemResponse;
import com.twistedmomos.backend.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MenuItemService {

    PageResponse<MenuItemResponse> search(Long categoryId, String q, Boolean veg, Integer maxSpicy, Pageable pageable);

    MenuItemResponse getPublicById(Long id);

    PageResponse<MenuItemResponse> adminList(Long categoryId, Pageable pageable);

    MenuItemResponse getAdminById(Long id);

    MenuItemResponse create(MenuItemRequest request);

    MenuItemResponse update(Long id, MenuItemRequest request);

    void delete(Long id);

    MenuItemResponse setAvailability(Long id, boolean available);

    MenuItemResponse uploadImage(Long id, MultipartFile file);
}

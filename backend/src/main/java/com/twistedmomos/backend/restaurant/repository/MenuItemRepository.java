package com.twistedmomos.backend.restaurant.repository;

import com.twistedmomos.backend.restaurant.entity.MenuItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {

    Optional<MenuItem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByCategoryId(Long categoryId);
}

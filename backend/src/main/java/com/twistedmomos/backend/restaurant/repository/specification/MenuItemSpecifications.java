package com.twistedmomos.backend.restaurant.repository.specification;

import com.twistedmomos.backend.restaurant.entity.MenuItem;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/** Composable predicates for GET /menu's optional categoryId/q/veg/maxSpicy query params. */
public final class MenuItemSpecifications {

    private MenuItemSpecifications() {
    }

    public static Specification<MenuItem> categoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<MenuItem> nameContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    public static Specification<MenuItem> isVeg(Boolean veg) {
        return (root, query, cb) -> veg == null ? null : cb.equal(root.get("veg"), veg);
    }

    public static Specification<MenuItem> maxSpicy(Integer maxSpicy) {
        return (root, query, cb) -> {
            if (maxSpicy == null) {
                return null;
            }
            return cb.or(cb.isNull(root.get("spicyLevel")), cb.lessThanOrEqualTo(root.get("spicyLevel"), maxSpicy));
        };
    }

    public static Specification<MenuItem> isAvailable(boolean available) {
        return (root, query, cb) -> cb.equal(root.get("available"), available);
    }

    /** Avoids N+1 category lookups when mapping a page of results — skipped automatically for Spring Data's internal COUNT query. */
    public static Specification<MenuItem> fetchCategory() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}

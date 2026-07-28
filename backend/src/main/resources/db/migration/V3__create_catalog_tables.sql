CREATE TABLE categories (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    slug          VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT uk_categories_slug UNIQUE (slug)
) ENGINE = InnoDB;

CREATE TABLE menu_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id   BIGINT NOT NULL,
    name          VARCHAR(150) NOT NULL,
    slug          VARCHAR(160) NOT NULL,
    description   VARCHAR(500),
    price         DECIMAL(10, 2) NOT NULL,
    image_url     VARCHAR(500),
    is_veg        BOOLEAN NOT NULL,
    spicy_level   INT,
    tag           VARCHAR(20),
    available     BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT uk_menu_items_slug UNIQUE (slug),
    CONSTRAINT fk_menu_items_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_menu_items_spicy_level CHECK (spicy_level IS NULL OR spicy_level BETWEEN 1 AND 3)
) ENGINE = InnoDB;

CREATE INDEX idx_menu_items_category_id ON menu_items (category_id);
CREATE INDEX idx_menu_items_available ON menu_items (available);

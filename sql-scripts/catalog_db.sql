CREATE DATABASE IF NOT EXISTS catalog_db;
USE catalog_db;

CREATE TABLE category (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    parent_id INT UNSIGNED NULL,
    FOREIGN KEY (parent_id) REFERENCES category(id)
);

CREATE TABLE unit_of_measure (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE vat_rate (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    rate_percentage DECIMAL(5, 2) NOT NULL,
    description VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE product (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category_id INT UNSIGNED NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    unit_id INT UNSIGNED NOT NULL,
    vat_rate_id INT UNSIGNED NOT NULL,
    weight DECIMAL(10, 3) NULL,
    package_dimensions JSON NULL,
    production_date DATE NULL,
    expiry_date DATE NULL,
    country_of_origin VARCHAR(100) NULL,
    status ENUM('draft', 'published', 'archived') NOT NULL DEFAULT 'draft',
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (unit_id) REFERENCES unit_of_measure(id),
    FOREIGN KEY (vat_rate_id) REFERENCES vat_rate(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_name (name),
    UNIQUE INDEX idx_supplier_sku (supplier_id, sku)
);

CREATE TABLE product_image (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT UNSIGNED NOT NULL,
    image_data BLOB NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE inventory (
    product_id BIGINT UNSIGNED PRIMARY KEY,
    quantity_available INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE events (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    version INT UNSIGNED NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_aggregate_id (aggregate_id),
    UNIQUE INDEX idx_aggregate_version (aggregate_id, version)
);
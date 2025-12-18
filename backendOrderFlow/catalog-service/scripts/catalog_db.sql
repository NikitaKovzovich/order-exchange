CREATE DATABASE IF NOT EXISTS catalog_db;
USE catalog_db;

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NULL,
    FOREIGN KEY (parent_id) REFERENCES category(id)
);

CREATE TABLE unit_of_measure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE vat_rate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rate_percentage DECIMAL(5, 2) NOT NULL,
    description VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category_id BIGINT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    unit_id BIGINT NOT NULL,
    vat_rate_id BIGINT NOT NULL,
    weight DECIMAL(10, 3) NULL,
    package_dimensions JSON NULL,
    production_date DATE NULL,
    expiry_date DATE NULL,
    country_of_origin VARCHAR(100) NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (unit_id) REFERENCES unit_of_measure(id),
    FOREIGN KEY (vat_rate_id) REFERENCES vat_rate(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_name (name),
    UNIQUE INDEX idx_supplier_sku (supplier_id, sku)
);

CREATE TABLE product_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE inventory (
    product_id BIGINT PRIMARY KEY,
    quantity_available INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    user_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_aggregate_type (aggregate_type),
    INDEX idx_created_at (created_at)
);

INSERT INTO unit_of_measure (name) VALUES ('шт'), ('кг'), ('л'), ('м'), ('уп');
INSERT INTO vat_rate (rate_percentage, description) VALUES (0, 'Без НДС'), (10, 'НДС 10%'), (20, 'НДС 20%');
INSERT INTO category (name, parent_id) VALUES ('Электроника', NULL), ('Продукты', NULL), ('Одежда', NULL);

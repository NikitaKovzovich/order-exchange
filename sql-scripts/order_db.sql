CREATE DATABASE IF NOT EXISTS order_db;
USE order_db;

CREATE TABLE order_status (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    status_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE orders (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    status_id INT UNSIGNED NOT NULL,
    delivery_address TEXT NOT NULL,
    desired_delivery_date DATE NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    vat_amount DECIMAL(12, 2) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (status_id) REFERENCES order_status(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_customer_id (customer_id)
);

CREATE TABLE order_item (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE order_history (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_description VARCHAR(255) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE order_document (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    document_type ENUM('payment_proof', 'signed_udd', 'discrepancy_report') NOT NULL,
    file_key VARCHAR(1024) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
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
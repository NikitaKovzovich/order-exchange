CREATE DATABASE IF NOT EXISTS order_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE order_db;

SET NAMES utf8mb4;

-- ============================================================
-- Справочник статусов заказа
-- ============================================================
CREATE TABLE order_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

INSERT INTO order_status (code, name, description) VALUES
('PENDING_CONFIRMATION', 'Ожидает подтверждения', 'Заказ создан и ожидает подтверждения поставщиком'),
('CONFIRMED', 'Подтвержден', 'Заказ подтвержден поставщиком (транзитный)'),
('REJECTED', 'Отклонен', 'Заказ отклонен поставщиком'),
('AWAITING_PAYMENT', 'Ожидает оплаты', 'Выставлен счет, ожидается оплата'),
('PENDING_PAYMENT_VERIFICATION', 'Ожидает проверки оплаты', 'Загружено платежное поручение, ожидается подтверждение'),
('PAID', 'Оплачен', 'Оплата подтверждена (транзитный)'),
('PAYMENT_PROBLEM', 'Проблема с оплатой', 'Оплата отклонена поставщиком'),
('AWAITING_SHIPMENT', 'Ожидает отгрузки', 'Заказ готовится к отгрузке'),
('SHIPPED', 'В пути', 'Заказ отправлен и находится в пути'),
('DELIVERED', 'Доставлен', 'Заказ получен покупателем'),
('AWAITING_CORRECTION', 'Ожидает корректировки', 'Выявлены расхождения при приемке'),
('CLOSED', 'Закрыт', 'Заказ закрыт'),
('CANCELLED', 'Отменен', 'Заказ отменен');

-- ============================================================
-- Заказы
-- ============================================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    delivery_address TEXT NOT NULL,
    desired_delivery_date DATE NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    vat_amount DECIMAL(12, 2) NOT NULL,
    payment_proof_key VARCHAR(1024) NULL,
    payment_reference VARCHAR(255) NULL,
    payment_notes TEXT NULL,
    contract_number VARCHAR(100) NULL,
    contract_date DATE NULL,
    contract_end_date DATE NULL,
    ttn_generated TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (status_id) REFERENCES order_status(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status_id (status_id),
    INDEX idx_order_number (order_number)
);

-- ============================================================
-- Позиции заказа
-- ============================================================
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    product_sku VARCHAR(100),
    unit_price DECIMAL(10, 2),
    vat_rate DECIMAL(5, 2) DEFAULT 0,
    quantity INT NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(12, 2),
    line_vat DECIMAL(12, 2),
    received_quantity INT,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ============================================================
-- История заказа (выровнена с entity OrderHistory)
-- ============================================================
CREATE TABLE order_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_description TEXT NOT NULL,
    user_id BIGINT,
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    metadata JSON,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_history_order_id (order_id)
);

-- ============================================================
-- Документы заказа (выровнена с entity OrderDocument)
-- ============================================================
CREATE TABLE order_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_key VARCHAR(1024) NOT NULL,
    original_filename VARCHAR(255),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_doc_order_id (order_id)
);

-- ============================================================
-- Расхождения при приёмке
-- ============================================================
CREATE TABLE order_discrepancy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    total_discrepancy_amount DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    resolved_at TIMESTAMP NULL,
    resolved_by BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE discrepancy_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    discrepancy_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    expected_quantity INT NOT NULL,
    actual_quantity INT NOT NULL,
    discrepancy_quantity INT NOT NULL,
    unit_price DECIMAL(10, 2),
    discrepancy_amount DECIMAL(12, 2),
    reason VARCHAR(50),
    FOREIGN KEY (discrepancy_id) REFERENCES order_discrepancy(id),
    FOREIGN KEY (order_item_id) REFERENCES order_item(id)
);

-- ============================================================
-- Корзина
-- ============================================================
CREATE TABLE cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    INDEX idx_cart_customer (customer_id)
);

CREATE TABLE cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    vat_rate DECIMAL(5, 2) DEFAULT 0,
    total_price DECIMAL(12, 2),
    vat_amount DECIMAL(12, 2),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(id),
    INDEX idx_cart_item_cart (cart_id),
    INDEX idx_cart_item_product (product_id)
);

-- ============================================================
-- Event Sourcing
-- ============================================================
CREATE TABLE events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    user_id BIGINT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_aggregate_type (aggregate_type),
    INDEX idx_event_type (event_type),
    INDEX idx_created_at (created_at)
);
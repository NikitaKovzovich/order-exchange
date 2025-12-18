CREATE DATABASE IF NOT EXISTS order_db;
USE order_db;

CREATE TABLE order_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

INSERT INTO order_status (code, name, description) VALUES
('PENDING_CONFIRMATION', 'Ожидает подтверждения', 'Заказ создан и ожидает подтверждения поставщиком'),
('CONFIRMED', 'Подтвержден', 'Заказ подтвержден поставщиком'),
('REJECTED', 'Отклонен', 'Заказ отклонен поставщиком'),
('AWAITING_PAYMENT', 'Ожидает оплаты', 'Выставлен счет, ожидается оплата'),
('PENDING_PAYMENT_VERIFICATION', 'Проверка оплаты', 'Загружено платежное поручение, ожидается подтверждение'),
('PAID', 'Оплачен', 'Оплата подтверждена'),
('AWAITING_SHIPMENT', 'Ожидает отгрузки', 'Заказ готовится к отгрузке'),
('SHIPPED', 'Отгружен', 'Заказ отправлен'),
('DELIVERED', 'Доставлен', 'Заказ получен покупателем'),
('AWAITING_CORRECTION', 'Требует корректировки', 'Выявлены расхождения при приемке'),
('CLOSED', 'Закрыт', 'Заказ закрыт'),
('CANCELLED', 'Отменен', 'Заказ отменен');

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (status_id) REFERENCES order_status(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status_id (status_id),
    INDEX idx_order_number (order_number)
);

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

CREATE TABLE order_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status_from VARCHAR(50),
    status_to VARCHAR(50),
    comment TEXT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE order_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255),
    file_key VARCHAR(1024) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

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
    INDEX idx_created_at (created_at)
);
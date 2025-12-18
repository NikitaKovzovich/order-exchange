CREATE DATABASE IF NOT EXISTS document_db;
USE document_db;

-- Типы документов
CREATE TABLE document_type (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- Документы
CREATE TABLE document (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    document_type_id INT UNSIGNED NOT NULL,
    entity_type VARCHAR(50) NOT NULL COMMENT 'order, company, verification',
    entity_id BIGINT UNSIGNED NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_key VARCHAR(1024) NOT NULL COMMENT 'S3/MinIO key',
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    uploaded_by BIGINT NOT NULL COMMENT 'user_id',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_type_id) REFERENCES document_type(id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_uploaded_by (uploaded_by)
);

-- Сгенерированные PDF документы
CREATE TABLE generated_document (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    template_type ENUM('invoice', 'upd', 'ttn', 'discrepancy_act') NOT NULL,
    order_id BIGINT UNSIGNED NOT NULL,
    file_key VARCHAR(1024) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by BIGINT NOT NULL COMMENT 'user_id or system',
    document_number VARCHAR(100) NOT NULL,
    document_date DATE NOT NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_template_type (template_type)
);

-- События для Event Sourcing
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

-- Начальные данные для типов документов
INSERT INTO document_type (code, name, description) VALUES
('INVOICE', 'Счет на оплату', 'Счет на оплату товаров'),
('PAYMENT_PROOF', 'Платежное поручение', 'Подтверждение оплаты'),
('UPD', 'Универсальный передаточный документ', 'УПД для отгрузки'),
('TTN', 'Товарно-транспортная накладная', 'ТТН для доставки'),
('DISCREPANCY_ACT', 'Акт о расхождении', 'Акт при несоответствии поставки'),
('SIGNED_UPD', 'Подписанный УПД', 'УПД с подписью получателя'),
('LOGO', 'Логотип компании', 'Логотип для документов'),
('REGISTRATION_CERT', 'Свидетельство о регистрации', 'Регистрационные документы'),
('CHARTER', 'Устав', 'Устав организации'),
('EDS', 'Электронная подпись', 'Файл ЭЦП');


CREATE DATABASE IF NOT EXISTS user_db;
USE user_db;

CREATE TABLE company (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    legal_name VARCHAR(255) NOT NULL,
    legal_form ENUM('IE', 'LLC', 'OJSC', 'CJSC', 'PJSC', 'PUE') NOT NULL,
    tax_id VARCHAR(20) NOT NULL UNIQUE,
    registration_date DATE NOT NULL,
    status ENUM('pending_verification', 'active', 'rejected', 'blocked') NOT NULL DEFAULT 'pending_verification',
    contact_phone VARCHAR(50) NULL
);

CREATE TABLE user (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    company_id BIGINT UNSIGNED UNIQUE,
    role ENUM('supplier', 'retail_chain', 'administrator') NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE address (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT UNSIGNED NOT NULL,
    address_type ENUM('legal', 'postal', 'shipping', 'delivery') NOT NULL,
    full_address TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE bank_account (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT UNSIGNED NOT NULL UNIQUE,
    bank_name VARCHAR(255) NOT NULL,
    bic VARCHAR(20) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE responsible_person (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT UNSIGNED NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    position ENUM('director', 'chief_accountant') NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id),
    UNIQUE INDEX idx_company_position (company_id, position)
);

CREATE TABLE company_document (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT UNSIGNED NOT NULL,
    document_type ENUM('registration_certificate', 'logo', 'eds_file', 'seal_image') NOT NULL,
    file_key VARCHAR(1024) NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE supplier_settings (
    company_id BIGINT UNSIGNED PRIMARY KEY,
    payment_terms ENUM('prepayment_100', 'payment_on_shipment') NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id)
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
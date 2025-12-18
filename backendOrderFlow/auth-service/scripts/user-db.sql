CREATE DATABASE IF NOT EXISTS user_db;
USE user_db;

CREATE TABLE company (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    legal_name VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    legal_form ENUM('IE', 'LLC', 'OJSC', 'CJSC', 'PJSC', 'PUE') NOT NULL,
    tax_id VARCHAR(20) NOT NULL UNIQUE,
    inn VARCHAR(20),
    registration_date DATE NOT NULL,
    status ENUM('PENDING_VERIFICATION', 'ACTIVE', 'REJECTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    contact_phone VARCHAR(50) NULL,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE users (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    company_id BIGINT UNSIGNED UNIQUE,
    role ENUM('SUPPLIER', 'RETAIL_CHAIN', 'ADMIN') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE verification_request (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    reviewer_id BIGINT UNSIGNED NULL,
    rejection_reason TEXT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
);

CREATE TABLE verification_document (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    verification_request_id BIGINT UNSIGNED NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    document_path VARCHAR(500) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (verification_request_id) REFERENCES verification_request(id)
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
    document_type ENUM('LOGO', 'REGISTRATION_CERTIFICATE', 'CHARTER', 'EDS_FILE', 'SEAL_IMAGE') NOT NULL,
    file_key VARCHAR(1024) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    original_filename VARCHAR(255),
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE supplier_settings (
    company_id BIGINT UNSIGNED PRIMARY KEY,
    payment_terms ENUM('prepayment_100', 'payment_on_shipment') NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE events (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    aggregate_id VARCHAR(255) ,
    aggregate_type VARCHAR(100) ,
    version INT UNSIGNED ,
    event_type VARCHAR(100) ,
    payload JSON ,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_aggregate_id (aggregate_id),
    UNIQUE INDEX idx_aggregate_version (aggregate_id, version)
);

CREATE DATABASE IF NOT EXISTS chat_db;
USE chat_db;

CREATE TABLE chat_channel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    supplier_user_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL,
    channel_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_supplier_user_id (supplier_user_id),
    INDEX idx_customer_user_id (customer_user_id)
);

CREATE TABLE message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    message_type ENUM('TEXT', 'FILE', 'SYSTEM') DEFAULT 'TEXT',
    attachment_key VARCHAR(1024),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES chat_channel(id),
    INDEX idx_channel_id (channel_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_sent_at (sent_at)
);

CREATE TABLE support_ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_company_id BIGINT NOT NULL,
    requester_user_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    status ENUM('NEW', 'IN_PROGRESS', 'WAITING_USER', 'RESOLVED', 'CLOSED') DEFAULT 'NEW',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    category ENUM('TECHNICAL', 'BILLING', 'ORDER', 'ACCOUNT', 'OTHER'),
    assigned_admin_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    INDEX idx_requester_company_id (requester_company_id),
    INDEX idx_status (status)
);

CREATE TABLE ticket_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    message_text TEXT NOT NULL,
    attachment_key VARCHAR(1024),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES support_ticket(id),
    INDEX idx_ticket_id (ticket_id)
);
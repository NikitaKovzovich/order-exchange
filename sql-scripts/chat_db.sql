CREATE DATABASE IF NOT EXISTS chat_db;
USE chat_db;

CREATE TABLE chat_channel (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    supplier_user_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL
);

CREATE TABLE message (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    channel_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES chat_channel(id)
);

CREATE TABLE support_ticket (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    requester_company_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    status ENUM('new', 'in_progress', 'resolved') NOT NULL DEFAULT 'new',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_message (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES support_ticket(id)
);
CREATE DATABASE IF NOT EXISTS ccis_sc_inventory;
USE ccis_sc_inventory;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('EXECUTIVE', 'COMMITTEE') NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(120) NOT NULL,
    description TEXT,
    quantity INT NOT NULL DEFAULT 0,
    image_path VARCHAR(255),
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_items_name ON items(item_name);
CREATE INDEX idx_items_quantity ON items(quantity);
CREATE INDEX idx_users_role ON users(role);

INSERT INTO users (username, password_hash, full_name, role, is_active)
VALUES ('admin', '$2a$10$o3Xv2asu5PnXanIN8SPime8o79OsPWcb6qJ.I8bzmy0oGVXyoqdAm', 'System Admin', 'EXECUTIVE', 1)
ON DUPLICATE KEY UPDATE username = username;

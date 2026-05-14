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

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(120) NOT NULL,
    description TEXT,
    quantity INT NOT NULL DEFAULT 0,
    min_quantity INT NOT NULL DEFAULT 0,
    image_path VARCHAR(255),
    category_id INT NOT NULL DEFAULT 1,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_user FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS item_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'ADJUST', 'ARCHIVE', 'RESTORE') NOT NULL,
    changed_by INT NOT NULL,
    old_name VARCHAR(120),
    new_name VARCHAR(120),
    old_description TEXT,
    new_description TEXT,
    old_quantity INT,
    new_quantity INT,
    old_category_id INT,
    new_category_id INT,
    old_min_quantity INT,
    new_min_quantity INT,
    old_is_archived TINYINT(1),
    new_is_archived TINYINT(1),
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users(id),
    CONSTRAINT fk_history_old_category FOREIGN KEY (old_category_id) REFERENCES categories(id),
    CONSTRAINT fk_history_new_category FOREIGN KEY (new_category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS login_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    logged_in_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80),
    entity_id INT,
    metadata VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_items_name ON items(item_name);
CREATE INDEX idx_items_quantity ON items(quantity);
CREATE INDEX idx_items_category ON items(category_id);
CREATE INDEX idx_items_archived ON items(is_archived);
CREATE INDEX idx_items_min_quantity ON items(min_quantity);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_history_item ON item_history(item_id);
CREATE INDEX idx_history_created ON item_history(created_at);
CREATE INDEX idx_login_user ON login_history(user_id);
CREATE INDEX idx_activity_user ON user_activity(user_id);

INSERT INTO users (username, password_hash, full_name, role, is_active)
VALUES ('admin', '$2a$10$o3Xv2asu5PnXanIN8SPime8o79OsPWcb6qJ.I8bzmy0oGVXyoqdAm', 'System Admin', 'EXECUTIVE', 1)
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO categories (name)
VALUES ('Electronics'), ('Supplies'), ('Furniture')
ON DUPLICATE KEY UPDATE name = name;

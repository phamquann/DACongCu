-- Tạo Database
CREATE DATABASE IF NOT EXISTS da_cong_cu;
USE da_cong_cu;

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- CATEGORIES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PRODUCTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    image_url VARCHAR(500),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_name (name),
    INDEX idx_category (category_id),
    INDEX idx_available (is_available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ORDERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    notes TEXT,
    delivery_address VARCHAR(255),
    delivery_date DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ORDER_ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- NOTIFICATIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Insert Categories
INSERT INTO categories (name, description) VALUES
('Burger', 'Các loại burger ngon tuyệt'),
('Pizza', 'Pizza với nhiều topping khác nhau'),
('Gà Rán', 'Gà rán giòn rụm'),
('Cơm', 'Các loại cơm'),
('Nước Uống', 'Nước ngọt và đồ uống');

-- Insert Sample Products
INSERT INTO products (name, description, price, quantity, category_id, image_url, is_available) VALUES
('Burger Bò', 'Burger thơm ngon với thịt bò tươi, xà lách, cà chua', 50000, 25, 1, NULL, TRUE),
('Burger Gà', 'Burger gà rán giòn với mayo và xà lách', 45000, 30, 1, NULL, TRUE),
('Burger Cộng Hòa', 'Burger đặc biệt với hai miếng thịt', 60000, 15, 1, NULL, TRUE),
('Pizza Hải Sản', 'Pizza với tôm, mực và cua', 120000, 10, 2, NULL, TRUE),
('Pizza Thịt', 'Pizza với thịt bò, xúc xích', 100000, 12, 2, NULL, TRUE),
('Pizza Vegetarian', 'Pizza với rau quả tươi', 85000, 20, 2, NULL, TRUE),
('Gà Rán 1 Miếng', 'Gà rán giòn, thơm lừng', 35000, 40, 3, NULL, TRUE),
('Gà Rán 3 Miếng', 'Bộ gà rán 3 miếng', 90000, 25, 3, NULL, TRUE),
('Cơm Gà Teriyaki', 'Cơm với gà teriyaki ngon', 45000, 50, 4, NULL, TRUE),
('Cơm Thịt Nướng', 'Cơm với thịt nướng', 50000, 40, 4, NULL, TRUE),
('Coca Cola', 'Nước ngọt 330ml', 12000, 100, 5, NULL, TRUE),
('Trà Chanh', 'Trà chanh tươi mát', 15000, 80, 5, NULL, TRUE);

-- Insert Sample Users
INSERT INTO users (name, email, phone, address) VALUES
('Phạm Văn B', 'pham.van.b@example.com', '0912345678', '123 Đường Lê Lợi, Q.1, TP.HCM'),
('Trần Thị C', 'tran.thi.c@example.com', '0912345679', '456 Đường Nguyễn Huệ, Q.1, TP.HCM'),
('Lê Văn D', 'le.van.d@example.com', '0912345680', '789 Đường Tôn Đức Thắng, Q.1, TP.HCM');

-- Insert Sample Notifications
INSERT INTO notifications (title, content, notification_type, is_read) VALUES
('Ưu đãi cuối tuần giảm 30%', 'Giảm 30% từ 100k cho đơn tối thiểu lựa chọn', 'INFO', FALSE),
('Cập nhật menu mới', 'Menu mới đã được cập nhật. Hãy xem các món ăn mới nhất!', 'SUCCESS', FALSE),
('Giao hàng miễn phí', 'Khi đặt hàng từ 200k trở lên, freeship cho bạn', 'INFO', FALSE);

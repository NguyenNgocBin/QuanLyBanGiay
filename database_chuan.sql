CREATE DATABASE IF NOT EXISTS `testlogin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `testlogin`;

-- Tắt kiểm tra khóa ngoại tạm thời để dễ dàng xóa bảng
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 1. BẢNG CATEGORIES (Danh mục sản phẩm - Đổi từ danhmuc)
-- =====================================================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 2. BẢNG PRODUCTS (Sản phẩm - Chuẩn hóa lại, thêm khóa ngoại)
-- =====================================================================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `product_code` VARCHAR(50) NOT NULL UNIQUE,  -- Ví dụ: #SH-1102
  `name` VARCHAR(255) NOT NULL,
  `category_id` INT,                           -- Khóa ngoại trỏ đến bảng categories
  `price` DOUBLE DEFAULT 0,
  `stock` INT DEFAULT 0,
  `size` VARCHAR(50),
  `image_path` VARCHAR(500),
  FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 3. BẢNG CUSTOMERS (Khách hàng - Chuẩn hóa tên cột)
-- =====================================================================
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `customer_code` VARCHAR(20) NOT NULL UNIQUE, -- Ví dụ: KH003
  `full_name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20),
  `email` VARCHAR(100) NOT NULL,
  `total_spent` DOUBLE DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 4. BẢNG ORDERS (Hóa đơn - Sửa lỗi Typo "oder", liên kết Khách hàng)
-- =====================================================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `customer_id` INT,                           -- Khóa ngoại trỏ đến khách hàng
  `total_amount` DOUBLE NOT NULL DEFAULT 0,
  `order_date` DATE DEFAULT (CURRENT_DATE),
  `status` VARCHAR(50) DEFAULT 'Chờ xử lý',
  FOREIGN KEY (`customer_id`) REFERENCES `customers`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 5. BẢNG ORDER_DETAILS (Chi tiết Hóa Đơn - THÊM MỚI QUAN TRỌNG)
-- =====================================================================
DROP TABLE IF EXISTS `order_details`;
CREATE TABLE `order_details` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `product_id` INT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `unit_price` DOUBLE NOT NULL,                -- Giá tại thời điểm mua
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 6. BẢNG USERS (Tài khoản nhân viên/quản lý)
-- =====================================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `email` VARCHAR(100) UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- INSERT DỮ LIỆU MẪU ĐỂ TEST
-- =====================================================================

INSERT INTO `categories` (`name`) VALUES 
('Giày Thể Thao'), ('Giày Tây'), ('Giày Sandal'), ('Phụ Kiện'), ('Giày Trẻ Em');

INSERT INTO `products` (`product_code`, `name`, `category_id`, `price`, `stock`, `size`) VALUES 
('#SH-1102', 'Puma RS-X', 1, 2500000, 5, '38-42'),
('#SH-2201', 'Oxford Classic Brown', 2, 4500000, 0, '41-43'),
('#SH-5541', 'Adidas Ultraboost 22', 1, 4200000, 15, '36-44'),
('#SH-8291', 'Nike Air Max 270', 1, 3800000, 45, '39-44');

INSERT INTO `customers` (`customer_code`, `full_name`, `phone`, `email`, `total_spent`) VALUES 
('KH003', 'Trần Hoàng Hải', '03839483943', 'hai@gmail.com', 2500000),
('KH004', 'Alest san đơ', '08472639288', 'alest@gmail.com', 4500000),
('KH005', 'Nguyễn Ngọc Bin', '0123456789', 'nguyenngocbin@gmail.com', 0);

-- Insert User (Giữ lại vài user mẫu có sẵn)
INSERT INTO `users` (`name`, `username`, `email`, `password`) VALUES 
('Admin User', 'admin', 'admin@example.com', 'adminpassword'),
('Phương Xinh', 'PhuongXinh', 'phuong@example.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');

-- Hóa đơn mẫu
INSERT INTO `orders` (`customer_id`, `total_amount`, `order_date`, `status`) VALUES 
(1, 2500000, '2026-05-01', 'Đã thanh toán'),
(2, 4500000, '2026-05-10', 'Đã thanh toán');

-- Chi tiết hóa đơn (Link Order 1 với Product 1, Order 2 với Product 2)
INSERT INTO `order_details` (`order_id`, `product_id`, `quantity`, `unit_price`) VALUES 
(1, 1, 1, 2500000),
(2, 2, 1, 4500000);

/*========XEM BANGR ===== */
select * from categories;
select * from orders;products
select * from categories;
select * from orders;
customers

delete from customers 
where id <= 2;


delete from orders where id = 4;



-- =======CAP NHAT NGAY 15-5 =====
USE `testlogin`;

-- Tắt kiểm tra khóa ngoại tạm thời
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- BẢNG SUPPLIERS (Nhà cung

-- 1. BẢNG SUPPLIERS (Nhà cung cấp)
CREATE TABLE IF NOT EXISTS `suppliers` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `supplier_code` VARCHAR(50) NOT NULL UNIQUE,
  `name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20),
  `email` VARCHAR(100),
  `address` VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. BẢNG IMPORT_ORDERS (Phiếu nhập hàng)
CREATE TABLE IF NOT EXISTS `import_orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `supplier_id` INT,
  `total_amount` DOUBLE NOT NULL DEFAULT 0,
  `import_date` DATE DEFAULT (CURRENT_DATE),
  `status` VARCHAR(50) DEFAULT 'Hoàn thành',
  FOREIGN KEY (`supplier_id`) REFERENCES `suppliers`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. BẢNG IMPORT_DETAILS (Chi tiết phiếu nhập)
CREATE TABLE IF NOT EXISTS `import_details` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `import_id` INT NOT NULL,
  `product_id` INT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `import_price` DOUBLE NOT NULL,
  FOREIGN KEY (`import_id`) REFERENCES `import_orders`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- THÊM DỮ LIỆU NHÀ CUNG CẤP MẪU
-- ==========================================
INSERT IGNORE INTO `suppliers` (`supplier_code`, `name`, `phone`, `email`, `address`) VALUES 
('NCC001', 'Công ty TNHH Giày Da Nike Việt Nam', '0283123456', 'contact@nike.vn', 'Quận 1, TP. Hồ Chí Minh'),
('NCC002', 'Đại lý phân phối Adidas miền Bắc', '0243987654', 'sales@adidas.com.vn', 'Cầu Giấy, Hà Nội'),
('NCC003', 'Xưởng sản xuất giày dép Bình Tân', '0909123999', 'xuonggiaybt@gmail.com', 'Bình Tân, TP. Hồ Chí Minh');

use testlogin;
-- ==========FIX LOI TIEN =====
ALTER TABLE products
MODIFY price DECIMAL(15,2);

ALTER TABLE customers
MODIFY total_spent DECIMAL(15,2);

ALTER TABLE orders
MODIFY total_amount DECIMAL(15,2);

ALTER TABLE import_orders
MODIFY total_amount DECIMAL(15,2);

ALTER TABLE import_details
MODIFY import_price DECIMAL(15,2);

ALTER TABLE order_details
MODIFY unit_price DECIMAL(15,2);


-- ====role phaan quyen ===
ALTER TABLE users
ADD role ENUM('ADMIN','STAFF') DEFAULT 'STAFF';

ALTER TABLE products
ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE customers
ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE orders
ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE users
ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE suppliers
ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
ON UPDATE CURRENT_TIMESTAMP;


ALTER TABLE customers
ADD CONSTRAINT unique_email UNIQUE(email);


ALTER TABLE customers
ADD CONSTRAINT unique_phone UNIQUE(phone);

-- kho hang
CREATE TABLE product_variants (
    id INT AUTO_INCREMENT PRIMARY KEY,

    product_id INT NOT NULL,

    size VARCHAR(10),
    color VARCHAR(50),

    stock INT DEFAULT 0,

    FOREIGN KEY(product_id)
    REFERENCES products(id)
    ON DELETE CASCADE
);

ALTER TABLE order_details
ADD variant_id INT;

ALTER TABLE order_details
ADD FOREIGN KEY (variant_id)
REFERENCES product_variants(id);

-- tu dong cho kho hang 
-- giam 
DELIMITER $$

CREATE TRIGGER trg_reduce_stock
AFTER INSERT ON order_details
FOR EACH ROW
BEGIN
    UPDATE product_variants
    SET stock = stock - NEW.quantity
    WHERE id = NEW.variant_id;
END$$

DELIMITER ;

-- tang - loi 

DELIMITER $$

CREATE TRIGGER trg_increase_stock
AFTER INSERT ON import_details
FOR EACH ROW
BEGIN
    UPDATE product_variants
    SET stock = stock + NEW.quantity
    WHERE id = NEW.variant_id;
END$$

DELIMITER ;

-- ====thanh toan 

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,

    order_id INT,

    payment_method ENUM(
        'CASH',
        'BANKING',
        'MOMO'
    ),

    amount DECIMAL(15,2),

    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(order_id)
    REFERENCES orders(id)
);


select * from users;
UPDATE users SET role = 'ADMIN' WHERE id = 3;


-- kiem soat nguoi nhap 
-- ==========================================
-- SOLEMANAGER SYSTEM DATABASE MIGRATION
-- ==========================================

-- 1. Create table inventory_logs to log all stock movements
CREATE TABLE IF NOT EXISTS inventory_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    old_stock INT NOT NULL,
    new_stock INT NOT NULL,
    change_qty INT NOT NULL,
    action_type VARCHAR(50) NOT NULL, -- 'IMPORT', 'SALE', 'MANUAL'
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 2. Trigger for logging stock increase after import
DROP TRIGGER IF EXISTS trg_after_insert_import_details;
DELIMITER //
CREATE TRIGGER trg_after_insert_import_details
AFTER INSERT ON import_details
FOR EACH ROW
BEGIN
    INSERT INTO inventory_logs (product_id, old_stock, new_stock, change_qty, action_type)
    SELECT NEW.product_id, p.stock - NEW.quantity, p.stock, NEW.quantity, 'IMPORT'
    FROM products p
    WHERE p.id = NEW.product_id;
END //
DELIMITER ;

-- 3. Trigger for logging stock decrease after sale
DROP TRIGGER IF EXISTS trg_after_insert_order_details;
DELIMITER //
CREATE TRIGGER trg_after_insert_order_details
AFTER INSERT ON order_details
FOR EACH ROW
BEGIN
    INSERT INTO inventory_logs (product_id, old_stock, new_stock, change_qty, action_type)
    SELECT NEW.product_id, p.stock + NEW.quantity, p.stock, -NEW.quantity, 'SALE'
    FROM products p
    WHERE p.id = NEW.product_id;
END //
DELIMITER ;

-- cap nhat bang users để lưu thông tin đang nhập của nhân viên 
use  testlogin;
ALTER TABLE users
ADD last_login DATETIME NULL,
ADD session_revenue DECIMAL(15,2) DEFAULT 0;

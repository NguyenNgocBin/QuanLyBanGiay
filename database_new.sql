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

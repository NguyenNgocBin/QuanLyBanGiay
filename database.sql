CREATE DATABASE  IF NOT EXISTS `testlogin` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `testlogin`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: testlogin
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `MaKH` varchar(10) NOT NULL,
  `HoTen` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `Sdt` varchar(11) DEFAULT NULL,
  `Email` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `TongchiTieu` double DEFAULT '0',
  PRIMARY KEY (`MaKH`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES ('KH003','Trần Hoàng Hải','03839483943','hai@gmail',0),('KH004','Alest san đơ','08472639288','alest@gmail.com',0),('KH005','có thể là fix bug đến chết','08472669288','welcom@gmail.com',0),('KH006','Thất Đại Số m','0293847564','that@gmail.com',0),('KH007','Thất Sát Kiếm','03758263849','kiemnhat@gmail.com',0),('KH008','Đế Vương 02','01234567385','devuongcacaz@gmail.com',0);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danhmuc`
--

DROP TABLE IF EXISTS `danhmuc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danhmuc` (
  `ID_DanhMuc` int NOT NULL AUTO_INCREMENT,
  `TenDanhMuc` varchar(100) NOT NULL,
  PRIMARY KEY (`ID_DanhMuc`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danhmuc`
--

LOCK TABLES `danhmuc` WRITE;
/*!40000 ALTER TABLE `danhmuc` DISABLE KEYS */;
INSERT INTO `danhmuc` VALUES (1,'Giày Thể Thao'),(2,'Giày Tây'),(3,'Giày Sandal'),(4,'Phụ Kiện'),(5,'Giày Trẻ Em');
/*!40000 ALTER TABLE `danhmuc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oder`
--

DROP TABLE IF EXISTS `oder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oder` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customer_name` varchar(100) NOT NULL,
  `total` decimal(15,0) NOT NULL,
  `order_date` date DEFAULT (curdate()),
  `status` varchar(50) DEFAULT 'Đã thanh toán',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oder`
--

LOCK TABLES `oder` WRITE;
/*!40000 ALTER TABLE `oder` DISABLE KEYS */;
INSERT INTO `oder` VALUES (1,'Nguyễn Văn A - Giày đi tiệc',2500000,'2026-01-05','Đã thanh toán'),(2,'Trần Thị B - Sỉ giày Sneaker',8500000,'2026-01-12','Đã thanh toán'),(3,'Lê Hoàng - Giày da nam',4200000,'2026-01-25','Đang giao hàng'),(4,'Phạm Thu - Quà Valentine',1800000,'2026-02-10','Đã thanh toán'),(5,'Cửa hàng X - Nhập bù hàng',5500000,'2026-02-18','Đã thanh toán'),(6,'Khách lẻ vãng lai',2100000,'2026-02-26','Đã thanh toán'),(7,'Vũ Minh - Bộ sưu tập Xuân',6800000,'2026-03-05','Đã thanh toán'),(8,'Ngô Quyền - Giày chạy bộ',3200000,'2026-03-15','Đã thanh toán'),(9,'Đặng Thùy - Sandal đi học',2500000,'2026-03-28','Đang giao hàng'),(10,'Đại lý Y - Lô dép du lịch',9500000,'2026-04-10','Đã thanh toán'),(11,'Nhóm phượt - Giày leo núi',4800000,'2026-04-20','Đã thanh toán'),(12,'Khách lẻ online',1900000,'2026-04-25','Đang giao hàng'),(13,'Lô giày trẻ em hè',7200000,'2026-05-08','Đã thanh toán'),(14,'Trần Hà - Sneaker trắng',3500000,'2026-05-15','Đã thanh toán'),(15,'Nguyễn Tùng - Giày lười',3800000,'2026-05-28','Chờ xử lý'),(16,'Resort Z - Dép đồng phục',12000000,'2026-06-05','Đã thanh toán'),(17,'Khách lẻ - Mua sắm hè',4500000,'2026-06-18','Đã thanh toán'),(18,'Hoàng Nam - Giày thể thao',1800000,'2026-06-25','Đã thanh toán'),(19,'Đơn sỉ nhỏ - Ủng đi mưa',5500000,'2026-07-10','Đang giao hàng'),(20,'Lê Lợi - Giày da chống nước',3200000,'2026-07-20','Đã thanh toán'),(21,'Phạm Hương - Boot cổ thấp',2800000,'2026-07-28','Đã thanh toán'),(22,'Trường Quốc Tế - Giày đồng phục',15500000,'2026-08-10','Đã thanh toán'),(23,'Hội phụ huynh - Mua chung',6800000,'2026-08-20','Đã thanh toán'),(24,'Sinh viên - Giày bata',1900000,'2026-08-28','Đã thanh toán'),(25,'Công sở A - Giày tây',7500000,'2026-09-08','Đã thanh toán'),(26,'Nguyễn Trãi - Giày thời trang',3500000,'2026-09-18','Đã thanh toán'),(27,'Khách vãng lai',2200000,'2026-09-25','Đã hủy'),(28,'Shop B - Nhập hàng Thu Đông',8200000,'2026-10-12','Đã thanh toán'),(29,'Trần Dần - Boot da',2800000,'2026-10-22','Đã thanh toán'),(30,'Khách lẻ',1500000,'2026-10-30','Đang giao hàng'),(31,'Săn Sale 11/11 - Đơn lớn',12500000,'2026-11-11','Đã thanh toán'),(32,'Black Friday - Sỉ',10500000,'2026-11-25','Đã thanh toán'),(33,'Khách lẻ săn sale',5200000,'2026-11-28','Đã thanh toán'),(37,'Nguyễn Văn A - Giày Sneaker',2500000,'2026-01-05','Đã thanh toán'),(38,'Nguyễn Văn KAKA - Giày Sneaker',2500000,'2026-01-05','Đã thanh toán'),(39,'Nguyễn Văn KAKA - Giày Sneaker',2500000,'2026-02-10','Đã thanh toán'),(40,'Nguyễn Ngọc Bin - Giày Tây',2500000,'2026-02-10','Đã thanh toán'),(41,'Nguyễn Ngọc Bin - Giày Lười',1500000,'2026-02-10','Đã thanh toán'),(42,'Nguyễn Ngọc Bin - Giày Bata',1500000,'2026-02-10','Chưa Thanh Toán');
/*!40000 ALTER TABLE `oder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `Id` varchar(50) NOT NULL,
  `Name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `Category` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Price` decimal(18,0) DEFAULT NULL,
  `Stock` int DEFAULT NULL,
  `Size` varchar(50) DEFAULT NULL,
  `Image_path` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES ('#SH-1102','Puma RS-X','Sneaker',2500000,5,'38-42',NULL),('#SH-2201','Oxford Classic Brown','Giày da',4500000,0,'41-43',NULL),('#SH-5541','Adidas Ultraboost 22','Running',4200000,15,'36-44',NULL),('#SH-8291','Nike Air Max 270','Sneaker',3800000,45,'39-44',NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Admin User','admin','adminpassword','2025-12-16 06:26:03'),(4,'daika','tothich','123456','2025-12-17 14:10:45'),(5,'Tớ vậy á','kakaka','123456789','2025-12-17 14:12:52'),(6,'CodeQuestor','thich','123456','2025-12-17 15:09:03'),(7,'kakaka','kakaakakk','123456','2025-12-17 15:09:59'),(8,'TayTo123','daikaz','123456','2025-12-17 15:12:16'),(9,'12rass','sadsadasds','ddd','2025-12-18 00:50:57'),(10,'kkaa','thichz','123456','2025-12-18 07:43:20'),(11,'phuong','1','1','2025-12-18 13:31:52'),(12,'k','2','2','2025-12-22 08:41:13'),(13,'h','khhhh','12345','2026-01-15 12:54:20'),(14,'kakazzz','67','67','2026-02-08 04:05:14'),(15,'phuong','phuong','c60d99a33a95e267399fcd1e73270c30603e797a39e651f11fa7314465009ae5','2026-02-10 14:22:05'),(16,'phuongdz','phuongdz','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','2026-02-10 14:22:23'),(17,'Phương Xinh Gái','PhuongXinh','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','2026-02-10 14:27:11'),(18,'Nguyễn Ngọc Zai','Zaidep','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','2026-02-10 14:33:59'),(19,'nguyễn Ngọc Bin','bin123456','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','2026-02-23 15:10:51');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-25 21:38:52

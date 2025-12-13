-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gym_system
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `course_id` int DEFAULT NULL,
  `booking_time` datetime NOT NULL,
  `booking_status` enum('pending','confirmed','cancelled') DEFAULT 'pending',
  PRIMARY KEY (`booking_id`),
  KEY `member_id` (`member_id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约表：存储会员对课程的预约记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `booking_detail_view`
--

DROP TABLE IF EXISTS `booking_detail_view`;
/*!50001 DROP VIEW IF EXISTS `booking_detail_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `booking_detail_view` AS SELECT 
 1 AS `booking_id`,
 1 AS `booking_time`,
 1 AS `booking_status`,
 1 AS `member_id`,
 1 AS `member_name`,
 1 AS `member_phone`,
 1 AS `course_id`,
 1 AS `course_name`,
 1 AS `course_type`,
 1 AS `duration`,
 1 AS `trainer_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `check_in`
--

DROP TABLE IF EXISTS `check_in`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `check_in` (
  `checkin_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `checkin_time` datetime NOT NULL,
  `checkout_time` datetime DEFAULT NULL,
  PRIMARY KEY (`checkin_id`),
  KEY `member_id` (`member_id`),
  CONSTRAINT `check_in_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='打卡表：记录会员的签到签退信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `check_in`
--

LOCK TABLES `check_in` WRITE;
/*!40000 ALTER TABLE `check_in` DISABLE KEYS */;
/*!40000 ALTER TABLE `check_in` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `course_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` enum('yoga','spinning','pilates','aerobics','strength','other') DEFAULT 'other',
  `duration` int NOT NULL COMMENT 'Course duration in minutes',
  `max_capacity` int NOT NULL DEFAULT '20',
  `employee_id` int DEFAULT NULL,
  `course_time` datetime DEFAULT NULL,
  PRIMARY KEY (`course_id`),
  KEY `employee_id` (`employee_id`),
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表：存储健身房提供的课程信息及对应教练';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'晨间瑜伽','yoga',60,10,3,'2025-12-14 18:43:42'),(2,'高强度燃脂','aerobics',45,15,3,'2025-12-15 18:43:42'),(3,'普拉提进阶','pilates',60,8,3,'2025-12-16 18:43:42');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `course_detail_view`
--

DROP TABLE IF EXISTS `course_detail_view`;
/*!50001 DROP VIEW IF EXISTS `course_detail_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `course_detail_view` AS SELECT 
 1 AS `course_id`,
 1 AS `course_name`,
 1 AS `type`,
 1 AS `duration`,
 1 AS `max_capacity`,
 1 AS `employee_id`,
 1 AS `trainer_name`,
 1 AS `trainer_phone`,
 1 AS `current_bookings`,
 1 AS `available_slots`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `current_members_in_gym`
--

DROP TABLE IF EXISTS `current_members_in_gym`;
/*!50001 DROP VIEW IF EXISTS `current_members_in_gym`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `current_members_in_gym` AS SELECT 
 1 AS `checkin_id`,
 1 AS `member_id`,
 1 AS `name`,
 1 AS `phone`,
 1 AS `checkin_time`,
 1 AS `minutes_in_gym`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `role_id` int DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `hire_date` date NOT NULL,
  PRIMARY KEY (`employee_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `employee_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `employee_role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表：存储健身房员工信息，包括教练、前台和管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'Admin',3,'1001','2023-01-01'),(2,'Alice(前台)',2,'1002','2023-05-01'),(3,'Bob(教练)',1,'1003','2023-06-01');
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee_role`
--

DROP TABLE IF EXISTS `employee_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_role` (
  `role_id` int NOT NULL,
  `role_name` varchar(50) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `permissions` varchar(500) DEFAULT NULL COMMENT '权限列表，逗号分隔',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工角色表：存储员工角色类型，体现继承关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee_role`
--

LOCK TABLES `employee_role` WRITE;
/*!40000 ALTER TABLE `employee_role` DISABLE KEYS */;
INSERT INTO `employee_role` VALUES (1,'Trainer','健身教练','course'),(2,'Receptionist','前台','all'),(3,'Admin','管理员','all');
/*!40000 ALTER TABLE `employee_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `gender` enum('male','female','other') DEFAULT 'other',
  `birth_date` date DEFAULT NULL,
  `register_date` datetime DEFAULT NULL,
  `status` enum('active','inactive','frozen') DEFAULT 'active',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员表：存储健身房会员的基本信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'张三','13800138000',NULL,'male',NULL,NULL,'active',500.00);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `member_detail_view`
--

DROP TABLE IF EXISTS `member_detail_view`;
/*!50001 DROP VIEW IF EXISTS `member_detail_view`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `member_detail_view` AS SELECT 
 1 AS `member_id`,
 1 AS `name`,
 1 AS `phone`,
 1 AS `email`,
 1 AS `gender`,
 1 AS `birth_date`,
 1 AS `age`,
 1 AS `register_date`,
 1 AS `member_status`,
 1 AS `card_id`,
 1 AS `type_id`,
 1 AS `type_name`,
 1 AS `start_date`,
 1 AS `end_date`,
 1 AS `card_status`,
 1 AS `days_remaining`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `membership_card`
--

DROP TABLE IF EXISTS `membership_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership_card` (
  `card_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `type_id` int DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `card_status` enum('active','inactive','expired') DEFAULT 'active',
  PRIMARY KEY (`card_id`),
  KEY `member_id` (`member_id`),
  KEY `type_id` (`type_id`),
  CONSTRAINT `membership_card_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `membership_card_ibfk_2` FOREIGN KEY (`type_id`) REFERENCES `membership_type` (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员卡表：存储会员的会籍卡信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership_card`
--

LOCK TABLES `membership_card` WRITE;
/*!40000 ALTER TABLE `membership_card` DISABLE KEYS */;
INSERT INTO `membership_card` VALUES (1,1,2,NULL,'2025-12-31','active');
/*!40000 ALTER TABLE `membership_card` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membership_type`
--

DROP TABLE IF EXISTS `membership_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership_type` (
  `type_id` int NOT NULL,
  `type_name` varchar(50) NOT NULL,
  `duration_days` int NOT NULL,
  `price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员卡类型表：存储会员卡类型，体现继承关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership_type`
--

LOCK TABLES `membership_type` WRITE;
/*!40000 ALTER TABLE `membership_type` DISABLE KEYS */;
INSERT INTO `membership_type` VALUES (1,'月卡',30,200.00,'30天'),(2,'年卡',365,1200.00,'365天');
/*!40000 ALTER TABLE `membership_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `monthly_checkin_stats`
--

DROP TABLE IF EXISTS `monthly_checkin_stats`;
/*!50001 DROP VIEW IF EXISTS `monthly_checkin_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `monthly_checkin_stats` AS SELECT 
 1 AS `member_id`,
 1 AS `name`,
 1 AS `year`,
 1 AS `month`,
 1 AS `checkin_count`,
 1 AS `avg_duration_minutes`,
 1 AS `last_checkin`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `order_type` enum('membership','product','course','recharge','renewal') NOT NULL DEFAULT 'product',
  `amount` decimal(10,2) NOT NULL,
  `order_time` datetime NOT NULL,
  `payment_status` enum('paid','unpaid','pending','paid_by_balance','paid_by_cash') NOT NULL DEFAULT 'paid',
  PRIMARY KEY (`order_id`),
  KEY `member_id` (`member_id`),
  CONSTRAINT `order_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表：存储所有类型的订单信息（会员卡、产品、课程）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_product`
--

DROP TABLE IF EXISTS `order_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_product` (
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`order_id`,`product_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `order_product_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`),
  CONSTRAINT `order_product_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单产品关联表：存储订单与产品的多对多关系及购买数量';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_product`
--

LOCK TABLES `order_product` WRITE;
/*!40000 ALTER TABLE `order_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品表：存储健身房销售的产品信息及库存';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'矿泉水',3.00,100),(2,'蛋白粉',450.00,20),(3,'毛巾',15.00,50);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `user_type` varchar(20) NOT NULL,
  `reference_id` int NOT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_reference` (`user_type`,`reference_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=','employee',1,'active','2025-12-13 10:43:42','2025-12-13 10:43:54','2025-12-13 10:43:54'),(2,'alice','jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=','employee',2,'active','2025-12-13 10:43:42','2025-12-13 10:43:42',NULL),(3,'bob','jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=','employee',3,'active','2025-12-13 10:43:42','2025-12-13 10:43:42',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `weekly_attendance`
--

DROP TABLE IF EXISTS `weekly_attendance`;
/*!50001 DROP VIEW IF EXISTS `weekly_attendance`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `weekly_attendance` AS SELECT 
 1 AS `member_id`,
 1 AS `week_num`,
 1 AS `total_checkins`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `booking_detail_view`
--

/*!50001 DROP VIEW IF EXISTS `booking_detail_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `booking_detail_view` AS select `b`.`booking_id` AS `booking_id`,`b`.`booking_time` AS `booking_time`,`b`.`booking_status` AS `booking_status`,`m`.`member_id` AS `member_id`,`m`.`name` AS `member_name`,`m`.`phone` AS `member_phone`,`c`.`course_id` AS `course_id`,`c`.`name` AS `course_name`,`c`.`type` AS `course_type`,`c`.`duration` AS `duration`,`e`.`name` AS `trainer_name` from (((`booking` `b` join `member` `m` on((`b`.`member_id` = `m`.`member_id`))) join `course` `c` on((`b`.`course_id` = `c`.`course_id`))) left join `employee` `e` on((`c`.`employee_id` = `e`.`employee_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `course_detail_view`
--

/*!50001 DROP VIEW IF EXISTS `course_detail_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `course_detail_view` AS select `c`.`course_id` AS `course_id`,`c`.`name` AS `course_name`,`c`.`type` AS `type`,`c`.`duration` AS `duration`,`c`.`max_capacity` AS `max_capacity`,`e`.`employee_id` AS `employee_id`,`e`.`name` AS `trainer_name`,`e`.`phone` AS `trainer_phone`,count(`b`.`booking_id`) AS `current_bookings`,(`c`.`max_capacity` - count(`b`.`booking_id`)) AS `available_slots` from ((`course` `c` left join `employee` `e` on((`c`.`employee_id` = `e`.`employee_id`))) left join `booking` `b` on(((`c`.`course_id` = `b`.`course_id`) and (`b`.`booking_status` = 'confirmed')))) group by `c`.`course_id`,`c`.`name`,`c`.`type`,`c`.`duration`,`c`.`max_capacity`,`e`.`employee_id`,`e`.`name`,`e`.`phone` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `current_members_in_gym`
--

/*!50001 DROP VIEW IF EXISTS `current_members_in_gym`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `current_members_in_gym` AS select `ci`.`checkin_id` AS `checkin_id`,`m`.`member_id` AS `member_id`,`m`.`name` AS `name`,`m`.`phone` AS `phone`,`ci`.`checkin_time` AS `checkin_time`,timestampdiff(MINUTE,`ci`.`checkin_time`,now()) AS `minutes_in_gym` from (`check_in` `ci` join `member` `m` on((`ci`.`member_id` = `m`.`member_id`))) where (`ci`.`checkout_time` is null) order by `ci`.`checkin_time` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `member_detail_view`
--

/*!50001 DROP VIEW IF EXISTS `member_detail_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `member_detail_view` AS select `m`.`member_id` AS `member_id`,`m`.`name` AS `name`,`m`.`phone` AS `phone`,`m`.`email` AS `email`,`m`.`gender` AS `gender`,`m`.`birth_date` AS `birth_date`,(year(curdate()) - year(`m`.`birth_date`)) AS `age`,`m`.`register_date` AS `register_date`,`m`.`status` AS `member_status`,`mc`.`card_id` AS `card_id`,`mc`.`type_id` AS `type_id`,`mt`.`type_name` AS `type_name`,`mc`.`start_date` AS `start_date`,`mc`.`end_date` AS `end_date`,`mc`.`card_status` AS `card_status`,(to_days(`mc`.`end_date`) - to_days(curdate())) AS `days_remaining` from ((`member` `m` left join `membership_card` `mc` on((`m`.`member_id` = `mc`.`member_id`))) left join `membership_type` `mt` on((`mc`.`type_id` = `mt`.`type_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `monthly_checkin_stats`
--

/*!50001 DROP VIEW IF EXISTS `monthly_checkin_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `monthly_checkin_stats` AS select `m`.`member_id` AS `member_id`,`m`.`name` AS `name`,year(`ci`.`checkin_time`) AS `year`,month(`ci`.`checkin_time`) AS `month`,count(0) AS `checkin_count`,avg(timestampdiff(MINUTE,`ci`.`checkin_time`,`ci`.`checkout_time`)) AS `avg_duration_minutes`,max(`ci`.`checkin_time`) AS `last_checkin` from (`member` `m` join `check_in` `ci` on((`m`.`member_id` = `ci`.`member_id`))) where (`ci`.`checkout_time` is not null) group by `m`.`member_id`,`m`.`name`,year(`ci`.`checkin_time`),month(`ci`.`checkin_time`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `weekly_attendance`
--

/*!50001 DROP VIEW IF EXISTS `weekly_attendance`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `weekly_attendance` AS select `check_in`.`member_id` AS `member_id`,week(`check_in`.`checkin_time`,0) AS `week_num`,count(0) AS `total_checkins` from `check_in` group by `check_in`.`member_id`,`week_num` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-13 18:47:57

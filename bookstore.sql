/*
 Navicat Premium Dump SQL

 Source Server         : BookStore
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : bookstore

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 22/12/2025 23:44:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for author
-- ----------------------------
DROP TABLE IF EXISTS `author`;
CREATE TABLE `author`  (
  `author_id` bigint NOT NULL AUTO_INCREMENT,
  `author_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nationality` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `biography` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`author_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of author
-- ----------------------------
INSERT INTO `author` VALUES (1, '王珊', '中国', '数据库系统概论教材作者之一。');
INSERT INTO `author` VALUES (2, 'Bruce Eckel', '美国', '《Thinking in Java》作者。');
INSERT INTO `author` VALUES (3, '李赛', '英国', '');

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `isbn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `publisher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `publish_date` date NULL DEFAULT NULL,
  `edition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL,
  `cover_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `catalog` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `series_flag` tinyint(1) NOT NULL DEFAULT 0,
  `parent_book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` enum('AVAILABLE','UNAVAILABLE') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'AVAILABLE',
  PRIMARY KEY (`book_id`) USING BTREE,
  UNIQUE INDEX `isbn`(`isbn` ASC) USING BTREE,
  INDEX `fk_book_parent`(`parent_book_id` ASC) USING BTREE,
  CONSTRAINT `fk_book_parent` FOREIGN KEY (`parent_book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `book_chk_1` CHECK (`price` >= 0)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book
-- ----------------------------
INSERT INTO `book` VALUES ('B001', '9787302290209', '数据库系统概论', '高等教育出版社', '2014-08-01', '第5版', 59.00, NULL, NULL, 0, NULL, 'AVAILABLE');
INSERT INTO `book` VALUES ('B002', '9787111213826', 'Java 编程思想', '机械工业出版社', '2007-06-01', '第4版', 108.00, NULL, NULL, 0, NULL, 'AVAILABLE');
INSERT INTO `book` VALUES ('B003', '9787121210979', 'Head First Java', '人民邮电出版社', '2012-01-01', '第2版', 79.00, NULL, NULL, 0, NULL, 'AVAILABLE');

-- ----------------------------
-- Table structure for book_author
-- ----------------------------
DROP TABLE IF EXISTS `book_author`;
CREATE TABLE `book_author`  (
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `author_id` bigint NOT NULL,
  `author_order` tinyint NOT NULL,
  PRIMARY KEY (`book_id`, `author_order`) USING BTREE,
  INDEX `fk_book_author_author`(`author_id` ASC) USING BTREE,
  CONSTRAINT `fk_book_author_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`author_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_book_author_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_author
-- ----------------------------
INSERT INTO `book_author` VALUES ('B001', 1, 1);
INSERT INTO `book_author` VALUES ('B002', 2, 1);
INSERT INTO `book_author` VALUES ('B001', 3, 2);

-- ----------------------------
-- Table structure for book_keyword
-- ----------------------------
DROP TABLE IF EXISTS `book_keyword`;
CREATE TABLE `book_keyword`  (
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `keyword_id` bigint NOT NULL,
  PRIMARY KEY (`book_id`, `keyword_id`) USING BTREE,
  INDEX `fk_book_keyword_keyword`(`keyword_id` ASC) USING BTREE,
  CONSTRAINT `fk_book_keyword_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_book_keyword_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`keyword_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_keyword
-- ----------------------------
INSERT INTO `book_keyword` VALUES ('B001', 1);
INSERT INTO `book_keyword` VALUES ('B002', 2);
INSERT INTO `book_keyword` VALUES ('B001', 3);
INSERT INTO `book_keyword` VALUES ('B001', 4);

-- ----------------------------
-- Table structure for credit_level
-- ----------------------------
DROP TABLE IF EXISTS `credit_level`;
CREATE TABLE `credit_level`  (
  `level_id` int NOT NULL AUTO_INCREMENT,
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `discount_rate` decimal(5, 4) NOT NULL,
  `allow_overdraft` tinyint(1) NOT NULL,
  `overdraft_limit` decimal(12, 2) NOT NULL,
  `upgrade_condition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`level_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of credit_level
-- ----------------------------
INSERT INTO `credit_level` VALUES (1, '一级', 0.9000, 0, 0.00, NULL);
INSERT INTO `credit_level` VALUES (2, '二级', 0.8500, 0, 0.00, NULL);
INSERT INTO `credit_level` VALUES (3, '三级', 0.8500, 1, 500.00, '可选：累计消费/余额达到某值');
INSERT INTO `credit_level` VALUES (4, '四级', 0.8000, 1, 2000.00, '可选：累计消费/余额达到某值');
INSERT INTO `credit_level` VALUES (5, '五级', 0.7500, 1, -1.00, '可选：累计消费/余额达到某值');
INSERT INTO `credit_level` VALUES (6, '一级', 0.9000, 0, 0.00, NULL);
INSERT INTO `credit_level` VALUES (7, '二级', 0.8500, 0, 0.00, NULL);
INSERT INTO `credit_level` VALUES (8, '三级', 0.8500, 1, 500.00, '可选：累计消费/余额达到某值');
INSERT INTO `credit_level` VALUES (9, '四级', 0.8000, 1, 2000.00, '可选：累计消费/余额达到某值');
INSERT INTO `credit_level` VALUES (10, '五级', 0.7500, 1, -1.00, '可选：累计消费/余额达到某值');

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer`  (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `mobile_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `account_balance` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `total_consumption` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `registration_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `account_status` enum('NORMAL','FROZEN') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NORMAL',
  `credit_level_id` int NOT NULL,
  PRIMARY KEY (`customer_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `fk_customer_credit_level`(`credit_level_id` ASC) USING BTREE,
  CONSTRAINT `fk_customer_credit_level` FOREIGN KEY (`credit_level_id`) REFERENCES `credit_level` (`level_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer
-- ----------------------------
INSERT INTO `customer` VALUES (1, 'zhangsan', 'test-hash-zhangsan', '张三', '13800000001', 'zhangsan@example.com', 132.50, 0.00, '2025-12-18 16:06:50', 'NORMAL', 3);
INSERT INTO `customer` VALUES (2, 'lisi', 'test-hash-lisi', '李四', '13800000002', 'lisi@example.com', 819.20, 0.00, '2025-12-18 16:06:50', 'NORMAL', 4);
INSERT INTO `customer` VALUES (3, 'wangwu', 'test-hash-wangwu', '王五', '13800000003', 'wangwu@example.com', 300.00, 0.00, '2025-12-18 16:14:13', 'NORMAL', 2);
INSERT INTO `customer` VALUES (4, 'kang', '123', '康晨煜', '18810108133', '18810108133@163.com', 710791175.45, 81702050.25, '2025-12-18 17:35:28', 'NORMAL', 5);

-- ----------------------------
-- Table structure for customer_address
-- ----------------------------
DROP TABLE IF EXISTS `customer_address`;
CREATE TABLE `customer_address`  (
  `address_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `receiver` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `idx_address_customer`(`customer_id` ASC) USING BTREE,
  CONSTRAINT `fk_address_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_address
-- ----------------------------
INSERT INTO `customer_address` VALUES (1, 4, '康晨煜', '18810108133', '湖北', '荆门', '京山', '格兰小城', 0);
INSERT INTO `customer_address` VALUES (2, 4, 'LadyK', '18810108133', 'HuBei', '湖北省 / 武汉市 / 洪山区', NULL, '珞喻路1037号', 1);

-- ----------------------------
-- Table structure for customer_notification
-- ----------------------------
DROP TABLE IF EXISTS `customer_notification`;
CREATE TABLE `customer_notification`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `order_id` bigint NULL DEFAULT NULL,
  `type` enum('OUT_OF_STOCK','PARTIAL_SHIPMENT','PROMOTION','SYSTEM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SYSTEM',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `read_flag` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_cn_customer`(`customer_id` ASC) USING BTREE,
  INDEX `fk_cn_order`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_cn_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_cn_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_notification
-- ----------------------------
INSERT INTO `customer_notification` VALUES (1, 4, 30, 'PARTIAL_SHIPMENT', '订单部分发货通知', '您的订单（30）已部分发货。 未发货：B001 未发 1 本；B002 未发 1 本；', '2025-12-19 16:30:21', 0);
INSERT INTO `customer_notification` VALUES (2, 4, 31, 'OUT_OF_STOCK', '缺书登记处理结果', '您的订单（31）的缺货登记未通过，订单已取消。', '2025-12-19 16:36:59', 0);
INSERT INTO `customer_notification` VALUES (3, 4, 24, 'PARTIAL_SHIPMENT', '订单部分发货通知', '您的订单（24）已部分发货。 未发货：B001 未发 23 本；', '2025-12-22 22:36:33', 0);
INSERT INTO `customer_notification` VALUES (4, 4, 40, 'OUT_OF_STOCK', '缺书登记处理结果', '您的订单（40）的缺货登记未通过，订单已取消。', '2025-12-22 22:52:09', 0);
INSERT INTO `customer_notification` VALUES (5, 4, 41, 'OUT_OF_STOCK', '缺书登记处理结果', '您的订单（41）的缺货登记已通过，请抓紧付款。', '2025-12-22 22:52:33', 0);

-- ----------------------------
-- Table structure for customer_out_of_stock_request
-- ----------------------------
DROP TABLE IF EXISTS `customer_out_of_stock_request`;
CREATE TABLE `customer_out_of_stock_request`  (
  `request_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `requested_qty` int NOT NULL,
  `customer_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `is_paid` tinyint(1) NOT NULL DEFAULT 0,
  `processed_status` enum('PENDING','ACCEPTED','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `related_record_id` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` datetime NULL DEFAULT NULL,
  `customer_notified` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`request_id`) USING BTREE,
  INDEX `fk_coor_customer`(`customer_id` ASC) USING BTREE,
  INDEX `fk_coor_book`(`book_id` ASC) USING BTREE,
  INDEX `idx_coor_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_coor_status`(`processed_status` ASC) USING BTREE,
  CONSTRAINT `fk_coor_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_coor_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_coor_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_out_of_stock_request
-- ----------------------------
INSERT INTO `customer_out_of_stock_request` VALUES (1, 11, 4, 'B001', 100000, '', 1, 'ACCEPTED', 1, '2025-12-18 21:01:09', NULL, 1);
INSERT INTO `customer_out_of_stock_request` VALUES (2, 13, 4, 'B002', 1000000, '', 0, 'REJECTED', NULL, '2025-12-18 21:17:50', '2025-12-18 21:18:18', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (3, 14, 4, 'B002', 10000, 'cnm', 0, 'ACCEPTED', 3, '2025-12-18 21:29:58', '2025-12-18 21:30:40', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (4, 15, 4, 'B002', 12345, '', 0, 'REJECTED', NULL, '2025-12-18 21:31:16', '2025-12-18 21:31:33', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (5, 16, 4, 'B002', 11222, 'nm', 0, 'ACCEPTED', 3, '2025-12-18 21:35:21', '2025-12-18 21:36:12', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (6, 17, 4, 'B002', 11111, 'cnm', 0, 'REJECTED', NULL, '2025-12-18 21:35:40', '2025-12-18 21:36:17', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (7, 19, 4, 'B002', 1000000, '', 0, 'REJECTED', NULL, '2025-12-18 21:46:17', '2025-12-18 21:46:36', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (8, 20, 4, 'B002', 100000, 'fhxj', 0, 'REJECTED', NULL, '2025-12-18 21:52:46', '2025-12-18 21:53:14', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (9, 21, 4, 'B001', 8999999, '', 1, 'ACCEPTED', 7, '2025-12-19 14:14:50', NULL, 1);
INSERT INTO `customer_out_of_stock_request` VALUES (10, 22, 4, 'B002', 10000000, '', 1, 'ACCEPTED', 3, '2025-12-19 14:15:11', NULL, 1);
INSERT INTO `customer_out_of_stock_request` VALUES (11, 23, 4, 'B001', 100000, '', 1, 'ACCEPTED', 7, '2025-12-19 14:22:30', '2025-12-19 14:22:30', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (12, 26, 4, 'B002', 14, '', 1, 'ACCEPTED', 3, '2025-12-19 15:55:11', '2025-12-19 15:55:11', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (13, 29, 4, 'B003', 1000, '', 0, 'ACCEPTED', 15, '2025-12-19 16:28:39', '2025-12-19 16:28:58', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (14, 31, 4, 'B003', 1000, '', 0, 'REJECTED', NULL, '2025-12-19 16:36:10', '2025-12-19 16:36:41', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (15, 38, 4, 'B003', 10000, '111', 1, 'ACCEPTED', 15, '2025-12-22 22:22:32', '2025-12-22 22:22:32', 1);
INSERT INTO `customer_out_of_stock_request` VALUES (16, 39, 4, 'B003', 10001, '222', 0, 'REJECTED', NULL, '2025-12-22 22:22:47', '2025-12-22 22:46:09', 0);
INSERT INTO `customer_out_of_stock_request` VALUES (17, 40, 4, 'B003', 100001, 'nihao', 0, 'REJECTED', NULL, '2025-12-22 22:52:01', '2025-12-22 22:52:09', 0);
INSERT INTO `customer_out_of_stock_request` VALUES (18, 41, 4, 'B003', 100001, 'nini', 0, 'ACCEPTED', 17, '2025-12-22 22:52:27', '2025-12-22 22:52:33', 0);

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT 0,
  `safety_stock` int NOT NULL DEFAULT 0,
  `location_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`book_id`) USING BTREE,
  CONSTRAINT `fk_inventory_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES ('B001', 45422312, 5, 'A-01-01');
INSERT INTO `inventory` VALUES ('B002', 20042457, 3, 'A-01-02');
INSERT INTO `inventory` VALUES ('B003', 1021, 2, 'A-01-03');

-- ----------------------------
-- Table structure for keyword
-- ----------------------------
DROP TABLE IF EXISTS `keyword`;
CREATE TABLE `keyword`  (
  `keyword_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`keyword_id`) USING BTREE,
  UNIQUE INDEX `keyword_text`(`keyword_text` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of keyword
-- ----------------------------
INSERT INTO `keyword` VALUES (4, 'code');
INSERT INTO `keyword` VALUES (2, 'Java');
INSERT INTO `keyword` VALUES (1, '数据库');
INSERT INTO `keyword` VALUES (3, '编程');

-- ----------------------------
-- Table structure for out_of_stock_record
-- ----------------------------
DROP TABLE IF EXISTS `out_of_stock_record`;
CREATE TABLE `out_of_stock_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `required_quantity` int NOT NULL,
  `record_date` date NOT NULL,
  `source` enum('MANUAL','LOW_STOCK','ORDER_EXCEED','CUSTOMER_REQUEST') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MANUAL',
  `related_customer_id` bigint NULL DEFAULT NULL,
  `status` enum('PENDING','PURCHASING','COMPLETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `priority` int NULL DEFAULT NULL,
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `fk_oos_customer`(`related_customer_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_oos_book_status`(`book_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_oos_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_oos_customer` FOREIGN KEY (`related_customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of out_of_stock_record
-- ----------------------------
INSERT INTO `out_of_stock_record` VALUES (1, 'B001', 100000, '2025-12-18', 'CUSTOMER_REQUEST', 4, 'COMPLETED', 1);
INSERT INTO `out_of_stock_record` VALUES (2, 'B002', 2, '2025-12-18', 'LOW_STOCK', NULL, 'COMPLETED', 1);
INSERT INTO `out_of_stock_record` VALUES (15, 'B003', 11000, '2025-12-22', 'CUSTOMER_REQUEST', 4, 'COMPLETED', 1);
INSERT INTO `out_of_stock_record` VALUES (17, 'B003', 100001, '2025-12-22', 'CUSTOMER_REQUEST', 4, 'PENDING', 1);
INSERT INTO `out_of_stock_record` VALUES (18, 'B002', 100, '2025-12-22', 'MANUAL', NULL, 'PENDING', 2);
INSERT INTO `out_of_stock_record` VALUES (19, 'B001', 100, '2025-12-22', 'MANUAL', NULL, 'PURCHASING', 1);

-- ----------------------------
-- Table structure for out_of_stock_record_backup
-- ----------------------------
DROP TABLE IF EXISTS `out_of_stock_record_backup`;
CREATE TABLE `out_of_stock_record_backup`  (
  `record_id` bigint NOT NULL DEFAULT 0,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `required_quantity` int NOT NULL,
  `record_date` date NOT NULL,
  `source` enum('MANUAL','LOW_STOCK','ORDER_EXCEED','CUSTOMER_REQUEST') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MANUAL',
  `related_customer_id` bigint NULL DEFAULT NULL,
  `status` enum('PENDING','PURCHASING','COMPLETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `priority` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of out_of_stock_record_backup
-- ----------------------------
INSERT INTO `out_of_stock_record_backup` VALUES (1, 'B001', 30, '2025-12-18', 'MANUAL', NULL, 'COMPLETED', 1);
INSERT INTO `out_of_stock_record_backup` VALUES (2, 'B001', 30, '2025-12-18', 'MANUAL', NULL, 'COMPLETED', 1);
INSERT INTO `out_of_stock_record_backup` VALUES (3, 'B001', 1000, '2025-12-18', 'MANUAL', NULL, 'COMPLETED', 1);

-- ----------------------------
-- Table structure for purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order`  (
  `purchase_order_id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint NOT NULL,
  `create_date` date NOT NULL,
  `expected_date` date NULL DEFAULT NULL,
  `buyer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `estimated_amount` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `status` enum('DRAFT','ISSUED','PARTIAL_RECEIVED','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT',
  PRIMARY KEY (`purchase_order_id`) USING BTREE,
  INDEX `fk_po_supplier`(`supplier_id` ASC) USING BTREE,
  CONSTRAINT `fk_po_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of purchase_order
-- ----------------------------
INSERT INTO `purchase_order` VALUES (1, 1, '2025-12-18', '2025-12-25', 'admin', 4000000.00, 'COMPLETED');
INSERT INTO `purchase_order` VALUES (2, 1, '2025-12-18', '2025-12-25', 'admin', 160.00, 'COMPLETED');
INSERT INTO `purchase_order` VALUES (3, 1, '2025-12-19', '2025-12-26', 'admin', 801700000.00, 'COMPLETED');
INSERT INTO `purchase_order` VALUES (4, 1, '2025-12-19', '2025-12-26', 'admin', 363999960.00, 'COMPLETED');
INSERT INTO `purchase_order` VALUES (5, 1, '2025-12-19', '2025-12-26', 'admin', 1200.00, 'COMPLETED');
INSERT INTO `purchase_order` VALUES (6, 2, '2025-12-22', NULL, 'admin', 660000.00, 'COMPLETED');
INSERT INTO `purchase_order` VALUES (7, 2, '2025-12-22', NULL, 'admin', 2000.00, 'ISSUED');

-- ----------------------------
-- Table structure for purchase_order_item
-- ----------------------------
DROP TABLE IF EXISTS `purchase_order_item`;
CREATE TABLE `purchase_order_item`  (
  `purchase_order_id` bigint NOT NULL,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `purchase_quantity` int NOT NULL,
  `purchase_price` decimal(10, 2) NOT NULL,
  `related_out_of_stock_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`purchase_order_id`, `book_id`) USING BTREE,
  INDEX `fk_poi_book`(`book_id` ASC) USING BTREE,
  INDEX `fk_poi_oos`(`related_out_of_stock_id` ASC) USING BTREE,
  CONSTRAINT `fk_poi_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_poi_oos` FOREIGN KEY (`related_out_of_stock_id`) REFERENCES `out_of_stock_record` (`record_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_poi_order` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_order` (`purchase_order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of purchase_order_item
-- ----------------------------
INSERT INTO `purchase_order_item` VALUES (1, 'B001', 100000, 40.00, 1);
INSERT INTO `purchase_order_item` VALUES (2, 'B002', 2, 80.00, 2);
INSERT INTO `purchase_order_item` VALUES (3, 'B002', 10021250, 80.00, 2);
INSERT INTO `purchase_order_item` VALUES (4, 'B001', 9099999, 40.00, 1);
INSERT INTO `purchase_order_item` VALUES (5, 'B001', 30, 40.00, 1);
INSERT INTO `purchase_order_item` VALUES (6, 'B003', 11000, 60.00, 15);
INSERT INTO `purchase_order_item` VALUES (7, 'B001', 100, 20.00, 19);

-- ----------------------------
-- Table structure for sales_order
-- ----------------------------
DROP TABLE IF EXISTS `sales_order`;
CREATE TABLE `sales_order`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `order_status` enum('PENDING_PAYMENT','OUT_OF_STOCK_PENDING','PENDING_SHIPMENT','DELIVERING','SHIPPED','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING_PAYMENT',
  `goods_amount` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `discount_rate_snapshot` decimal(5, 4) NOT NULL,
  `payable_amount` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `shipping_address_snapshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `payment_time` datetime NULL DEFAULT NULL,
  `delivery_time` datetime NULL DEFAULT NULL,
  `customer_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_sales_order_customer`(`customer_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sales_order
-- ----------------------------
INSERT INTO `sales_order` VALUES (1, 1, '2025-12-18 16:20:19', 'SHIPPED', 233.75, 0.8500, 233.75, '张三, 北京市海淀区 某路某号, 13800000001', '2025-12-18 16:24:39', '2025-12-18 17:21:42', '测试下单');
INSERT INTO `sales_order` VALUES (2, 2, '2025-12-18 17:06:24', 'SHIPPED', 180.80, 0.8000, 180.80, '李四, 测试地址, 13800000002', '2025-12-18 17:06:24', '2025-12-18 17:06:24', NULL);
INSERT INTO `sales_order` VALUES (3, 4, '2025-12-18 17:36:15', 'SHIPPED', 159.30, 0.9000, 159.30, '康晨煜, 18810108133', '2025-12-18 17:51:14', '2025-12-18 17:59:58', NULL);
INSERT INTO `sales_order` VALUES (4, 4, '2025-12-18 17:44:26', 'PENDING_SHIPMENT', 125.25, 0.7500, 125.25, '康晨煜, 18810108133', '2025-12-18 17:44:27', NULL, NULL);
INSERT INTO `sales_order` VALUES (5, 4, '2025-12-18 18:15:35', 'PENDING_SHIPMENT', 1060.50, 0.7500, 1060.50, '康晨煜, 18810108133', '2025-12-18 18:15:41', NULL, NULL);
INSERT INTO `sales_order` VALUES (6, 4, '2025-12-18 19:30:08', 'PENDING_SHIPMENT', 132.75, 0.7500, 132.75, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 19:30:13', NULL, NULL);
INSERT INTO `sales_order` VALUES (7, 4, '2025-12-18 20:13:31', 'PENDING_SHIPMENT', 44.25, 0.7500, 44.25, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 20:41:09', NULL, NULL);
INSERT INTO `sales_order` VALUES (8, 4, '2025-12-18 20:55:30', 'PENDING_SHIPMENT', 442500.00, 0.7500, 442500.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 20:55:44', NULL, NULL);
INSERT INTO `sales_order` VALUES (9, 4, '2025-12-18 20:55:53', 'PENDING_SHIPMENT', 98982.00, 0.7500, 98982.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 21:00:52', NULL, NULL);
INSERT INTO `sales_order` VALUES (10, 4, '2025-12-18 20:57:22', 'SHIPPED', 54073.50, 0.7500, 54073.50, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 20:57:32', '2025-12-18 21:02:39', NULL);
INSERT INTO `sales_order` VALUES (11, 4, '2025-12-18 21:01:04', 'SHIPPED', 4425000.00, 0.7500, 4425000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 21:01:11', '2025-12-18 21:02:31', NULL);
INSERT INTO `sales_order` VALUES (12, 4, '2025-12-18 21:03:13', 'SHIPPED', 1296.00, 0.7500, 1296.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 21:03:14', '2025-12-18 21:03:35', NULL);
INSERT INTO `sales_order` VALUES (13, 4, '2025-12-18 21:17:46', 'PENDING_PAYMENT', 81000000.00, 0.7500, 81000000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (14, 4, '2025-12-18 21:29:51', 'PENDING_SHIPMENT', 810000.00, 0.7500, 810000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 21:30:40', NULL, NULL);
INSERT INTO `sales_order` VALUES (15, 4, '2025-12-18 21:31:14', 'PENDING_SHIPMENT', 999945.00, 0.7500, 999945.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-18 21:35:28', NULL, NULL);
INSERT INTO `sales_order` VALUES (16, 4, '2025-12-18 21:35:11', 'PENDING_SHIPMENT', 908982.00, 0.7500, 908982.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (17, 4, '2025-12-18 21:35:35', 'CANCELLED', 899991.00, 0.7500, 899991.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (18, 4, '2025-12-18 21:45:54', 'PENDING_SHIPMENT', 81000000.00, 0.7500, 81000000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 15:00:10', NULL, NULL);
INSERT INTO `sales_order` VALUES (19, 4, '2025-12-18 21:46:16', 'CANCELLED', 81000000.00, 0.7500, 81000000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (20, 4, '2025-12-18 21:52:42', 'CANCELLED', 8100000.00, 0.7500, 8100000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (21, 4, '2025-12-19 14:14:46', 'PENDING_SHIPMENT', 398249955.75, 0.7500, 398249955.75, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 14:14:53', NULL, NULL);
INSERT INTO `sales_order` VALUES (22, 4, '2025-12-19 14:15:09', 'PENDING_SHIPMENT', 810000000.00, 0.7500, 810000000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 14:15:12', NULL, NULL);
INSERT INTO `sales_order` VALUES (23, 4, '2025-12-19 14:22:27', 'DELIVERING', 4425000.00, 0.7500, 4425000.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 14:22:32', '2025-12-19 15:56:11', NULL);
INSERT INTO `sales_order` VALUES (24, 4, '2025-12-19 14:30:14', 'DELIVERING', 1017.75, 0.7500, 1017.75, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 14:30:16', '2025-12-22 22:36:33', NULL);
INSERT INTO `sales_order` VALUES (25, 4, '2025-12-19 14:52:22', 'COMPLETED', 81.00, 0.7500, 81.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 14:52:23', NULL, NULL);
INSERT INTO `sales_order` VALUES (26, 4, '2025-12-19 15:55:08', 'COMPLETED', 1665.00, 0.7500, 1665.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 15:55:13', NULL, NULL);
INSERT INTO `sales_order` VALUES (27, 4, '2025-12-19 16:13:31', 'COMPLETED', 568.50, 0.7500, 568.50, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 16:13:33', NULL, NULL);
INSERT INTO `sales_order` VALUES (28, 4, '2025-12-19 16:21:29', 'DELIVERING', 287.25, 0.7500, 287.25, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 16:21:30', NULL, NULL);
INSERT INTO `sales_order` VALUES (29, 4, '2025-12-19 16:28:36', 'PENDING_SHIPMENT', 59250.00, 0.7500, 59250.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-22 21:45:09', NULL, NULL);
INSERT INTO `sales_order` VALUES (30, 4, '2025-12-19 16:29:38', 'DELIVERING', 125.25, 0.7500, 125.25, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 16:29:39', '2025-12-19 16:30:21', NULL);
INSERT INTO `sales_order` VALUES (31, 4, '2025-12-19 16:36:07', 'CANCELLED', 59250.00, 0.7500, 59250.00, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (32, 4, '2025-12-19 16:41:50', 'COMPLETED', 2305.50, 0.7500, 2305.50, '康晨煜 / 18810108133 - 湖北荆门京山格兰小城 [默认]', '2025-12-19 16:41:51', NULL, NULL);
INSERT INTO `sales_order` VALUES (33, 4, '2025-12-22 21:55:14', 'PENDING_PAYMENT', 44.25, 0.7500, 44.25, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (34, 4, '2025-12-22 21:55:22', 'PENDING_PAYMENT', 4425000.00, 0.7500, 4425000.00, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (35, 4, '2025-12-22 22:21:18', 'PENDING_PAYMENT', 442898.25, 0.7500, 442898.25, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (36, 4, '2025-12-22 22:21:29', 'PENDING_PAYMENT', 81000000.00, 0.7500, 81000000.00, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (37, 4, '2025-12-22 22:22:04', 'PENDING_PAYMENT', 442500.00, 0.7500, 442500.00, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (38, 4, '2025-12-22 22:22:16', 'DELIVERING', 592500.00, 0.7500, 592500.00, '康晨煜', '2025-12-22 22:22:32', '2025-12-22 22:48:25', NULL);
INSERT INTO `sales_order` VALUES (39, 4, '2025-12-22 22:22:44', 'CANCELLED', 592559.25, 0.7500, 592559.25, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (40, 4, '2025-12-22 22:51:48', 'CANCELLED', 5925059.25, 0.7500, 5925059.25, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (41, 4, '2025-12-22 22:52:22', 'PENDING_PAYMENT', 5925059.25, 0.7500, 5925059.25, '康晨煜', NULL, NULL, NULL);
INSERT INTO `sales_order` VALUES (42, 4, '2025-12-22 22:56:27', 'PENDING_SHIPMENT', 44250.00, 0.7500, 44250.00, '康晨煜', '2025-12-22 22:56:34', NULL, NULL);
INSERT INTO `sales_order` VALUES (43, 4, '2025-12-22 22:56:45', 'DELIVERING', 531.00, 0.7500, 531.00, '康晨煜', NULL, '2025-12-22 22:57:18', NULL);

-- ----------------------------
-- Table structure for sales_order_item
-- ----------------------------
DROP TABLE IF EXISTS `sales_order_item`;
CREATE TABLE `sales_order_item`  (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `quantity` int NOT NULL,
  `shipped_quantity` int NOT NULL DEFAULT 0,
  `received_quantity` int NOT NULL DEFAULT 0,
  `unit_price` decimal(10, 2) NOT NULL,
  `sub_amount` decimal(12, 2) NOT NULL,
  `item_status` enum('ORDERED','PART_SHIPPED','SHIPPED','RECEIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ORDERED',
  PRIMARY KEY (`order_item_id`) USING BTREE,
  INDEX `fk_item_book`(`book_id` ASC) USING BTREE,
  INDEX `idx_sales_order_item_order`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sales_order_item
-- ----------------------------
INSERT INTO `sales_order_item` VALUES (1, 1, 'B001', 1, 0, 0, 50.15, 50.15, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (2, 1, 'B002', 2, 0, 0, 91.80, 183.60, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (3, 2, 'B001', 2, 0, 0, 47.20, 94.40, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (4, 2, 'B002', 1, 0, 0, 86.40, 86.40, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (5, 3, 'B001', 3, 0, 0, 53.10, 159.30, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (6, 4, 'B001', 1, 0, 0, 44.25, 44.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (7, 4, 'B002', 1, 0, 0, 81.00, 81.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (8, 5, 'B001', 2, 0, 0, 44.25, 88.50, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (9, 5, 'B002', 12, 0, 0, 81.00, 972.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (10, 6, 'B001', 3, 0, 0, 44.25, 132.75, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (11, 7, 'B001', 1, 0, 0, 44.25, 44.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (12, 8, 'B001', 10000, 0, 0, 44.25, 442500.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (13, 9, 'B002', 1222, 0, 0, 81.00, 98982.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (14, 10, 'B001', 1222, 0, 0, 44.25, 54073.50, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (15, 11, 'B001', 100000, 0, 0, 44.25, 4425000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (16, 12, 'B002', 16, 0, 0, 81.00, 1296.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (17, 13, 'B002', 1000000, 0, 0, 81.00, 81000000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (18, 14, 'B002', 10000, 0, 0, 81.00, 810000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (19, 15, 'B002', 12345, 0, 0, 81.00, 999945.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (20, 16, 'B002', 11222, 0, 0, 81.00, 908982.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (21, 17, 'B002', 11111, 0, 0, 81.00, 899991.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (22, 18, 'B002', 1000000, 0, 0, 81.00, 81000000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (23, 19, 'B002', 1000000, 0, 0, 81.00, 81000000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (24, 20, 'B002', 100000, 0, 0, 81.00, 8100000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (25, 21, 'B001', 8999999, 0, 0, 44.25, 398249955.75, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (26, 22, 'B002', 10000000, 0, 0, 81.00, 810000000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (27, 23, 'B001', 100000, 100000, 0, 44.25, 4425000.00, 'SHIPPED');
INSERT INTO `sales_order_item` VALUES (28, 24, 'B001', 23, 2, 0, 44.25, 1017.75, 'PART_SHIPPED');
INSERT INTO `sales_order_item` VALUES (29, 25, 'B002', 1, 2, 2, 81.00, 81.00, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (30, 26, 'B001', 12, 12, 12, 44.25, 531.00, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (31, 26, 'B002', 14, 14, 14, 81.00, 1134.00, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (32, 27, 'B001', 2, 2, 2, 44.25, 88.50, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (33, 27, 'B002', 3, 3, 3, 81.00, 243.00, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (34, 27, 'B003', 4, 4, 4, 59.25, 237.00, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (35, 28, 'B001', 1, 1, 1, 44.25, 44.25, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (36, 28, 'B002', 3, 3, 1, 81.00, 243.00, 'PART_SHIPPED');
INSERT INTO `sales_order_item` VALUES (37, 29, 'B003', 1000, 0, 0, 59.25, 59250.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (38, 30, 'B001', 1, 0, 0, 44.25, 44.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (39, 30, 'B002', 1, 1, 0, 81.00, 81.00, 'SHIPPED');
INSERT INTO `sales_order_item` VALUES (40, 31, 'B003', 1000, 0, 0, 59.25, 59250.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (41, 32, 'B001', 10, 10, 10, 44.25, 442.50, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (42, 32, 'B002', 23, 23, 23, 81.00, 1863.00, 'RECEIVED');
INSERT INTO `sales_order_item` VALUES (43, 33, 'B001', 1, 0, 0, 44.25, 44.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (44, 34, 'B001', 100000, 0, 0, 44.25, 4425000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (45, 35, 'B001', 10009, 0, 0, 44.25, 442898.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (46, 36, 'B002', 1000000, 0, 0, 81.00, 81000000.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (47, 37, 'B001', 10000, 0, 0, 44.25, 442500.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (48, 38, 'B003', 10000, 10000, 0, 59.25, 592500.00, 'SHIPPED');
INSERT INTO `sales_order_item` VALUES (49, 39, 'B003', 10001, 0, 0, 59.25, 592559.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (50, 40, 'B003', 100001, 0, 0, 59.25, 5925059.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (51, 41, 'B003', 100001, 0, 0, 59.25, 5925059.25, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (52, 42, 'B001', 1000, 0, 0, 44.25, 44250.00, 'ORDERED');
INSERT INTO `sales_order_item` VALUES (53, 43, 'B001', 12, 12, 0, 44.25, 531.00, 'SHIPPED');

-- ----------------------------
-- Table structure for shipment
-- ----------------------------
DROP TABLE IF EXISTS `shipment`;
CREATE TABLE `shipment`  (
  `shipment_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `ship_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `carrier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tracking_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `shipment_status` enum('SHIPPED','IN_TRANSIT','DELIVERED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SHIPPED',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`shipment_id`) USING BTREE,
  INDEX `fk_shipment_order`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_shipment_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shipment
-- ----------------------------
INSERT INTO `shipment` VALUES (1, 1, '2025-12-18 16:42:17', '顺丰速运', 'SF123456789', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (2, 2, '2025-12-18 17:06:24', '中通快递', 'ZTO1766048784065', 'SHIPPED', '发货员小李');
INSERT INTO `shipment` VALUES (3, 1, '2025-12-18 17:21:42', '顺丰速运', 'SF1766049697111', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (4, 3, '2025-12-18 17:59:58', '顺丰速运', 'SF1766051996658', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (5, 11, '2025-12-18 21:02:31', '顺丰速运', 'SF1766062949564', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (6, 10, '2025-12-18 21:02:39', '顺丰速运', 'SF1766062958059', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (7, 12, '2025-12-18 21:03:35', '顺丰速运', 'SF1766063013924', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (8, 25, '2025-12-19 14:53:02', '顺丰速运', 'SF1766127180596', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (9, 25, '2025-12-19 14:54:28', '顺丰速运', 'SF1766127266924', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (10, 26, '2025-12-19 15:56:04', '顺丰速运', 'SF1766130962822', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (11, 23, '2025-12-19 15:56:11', '顺丰速运', 'SF1766130969960', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (12, 27, '2025-12-19 16:14:20', '顺丰速运', 'SF1766132050733', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (13, 27, '2025-12-19 16:15:29', '顺丰速运', 'SF1766132121299', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (14, 28, '2025-12-19 16:21:53', '顺丰速运', 'SF1766132511585', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (15, 30, '2025-12-19 16:30:21', '顺丰速运', 'SF1766133016580', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (16, 32, '2025-12-19 16:42:06', '顺丰速运', 'SF1766133724543', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (17, 24, '2025-12-22 22:36:33', '顺丰', '123', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (18, 38, '2025-12-22 22:48:25', '顺丰', '1111', 'SHIPPED', 'admin');
INSERT INTO `shipment` VALUES (19, 43, '2025-12-22 22:57:18', '顺丰', '1', 'SHIPPED', 'admin');

-- ----------------------------
-- Table structure for shipment_item
-- ----------------------------
DROP TABLE IF EXISTS `shipment_item`;
CREATE TABLE `shipment_item`  (
  `shipment_item_id` bigint NOT NULL AUTO_INCREMENT,
  `shipment_id` bigint NOT NULL,
  `order_item_id` bigint NOT NULL,
  `ship_quantity` int NOT NULL,
  `receive_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `received_quantity` int NOT NULL DEFAULT 0,
  `received_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`shipment_item_id`) USING BTREE,
  INDEX `fk_shipment_item_shipment`(`shipment_id` ASC) USING BTREE,
  INDEX `idx_shipment_item_order_item`(`order_item_id` ASC) USING BTREE,
  CONSTRAINT `fk_shipment_item_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `sales_order_item` (`order_item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_shipment_item_shipment` FOREIGN KEY (`shipment_id`) REFERENCES `shipment` (`shipment_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shipment_item
-- ----------------------------
INSERT INTO `shipment_item` VALUES (1, 1, 1, 1, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (2, 1, 2, 2, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (3, 2, 3, 2, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (4, 2, 4, 1, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (5, 3, 1, 1, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (6, 3, 2, 2, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (7, 4, 5, 3, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (8, 5, 15, 100000, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (9, 6, 14, 1222, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (10, 7, 16, 16, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (11, 8, 29, 1, 'RECEIVED', 1, '2025-12-19 15:54:44');
INSERT INTO `shipment_item` VALUES (12, 9, 29, 1, 'RECEIVED', 1, '2025-12-19 15:54:44');
INSERT INTO `shipment_item` VALUES (13, 10, 30, 12, 'RECEIVED', 12, '2025-12-19 16:13:46');
INSERT INTO `shipment_item` VALUES (14, 10, 31, 14, 'RECEIVED', 14, '2025-12-19 16:13:46');
INSERT INTO `shipment_item` VALUES (15, 11, 27, 100000, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (16, 12, 33, 3, 'RECEIVED', 3, '2025-12-19 16:16:06');
INSERT INTO `shipment_item` VALUES (17, 12, 34, 4, 'RECEIVED', 4, '2025-12-19 16:16:06');
INSERT INTO `shipment_item` VALUES (18, 13, 32, 2, 'RECEIVED', 2, '2025-12-19 16:16:21');
INSERT INTO `shipment_item` VALUES (19, 14, 35, 1, 'RECEIVED', 1, '2025-12-22 22:08:35');
INSERT INTO `shipment_item` VALUES (20, 14, 36, 3, 'PENDING', 1, NULL);
INSERT INTO `shipment_item` VALUES (21, 15, 39, 1, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (22, 16, 41, 10, 'RECEIVED', 10, '2025-12-19 16:42:43');
INSERT INTO `shipment_item` VALUES (23, 16, 42, 23, 'RECEIVED', 23, '2025-12-19 16:42:43');
INSERT INTO `shipment_item` VALUES (24, 17, 28, 2, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (25, 18, 48, 10000, 'PENDING', 0, NULL);
INSERT INTO `shipment_item` VALUES (26, 19, 53, 12, 'PENDING', 0, NULL);

-- ----------------------------
-- Table structure for supplier
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier`  (
  `supplier_id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `contact_person` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `payment_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `cooperation_status` enum('ACTIVE','SUSPENDED','TERMINATED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`supplier_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of supplier
-- ----------------------------
INSERT INTO `supplier` VALUES (1, '新华图书批发中心', '王经理', '010-88888888', 'boss@xinhua.com', '北京市海淀区某路1号', '月结30天', 'ACTIVE');
INSERT INTO `supplier` VALUES (2, '高校教材供应站', '李老师', '010-66666666', 'textbook@supplier.com', '北京市朝阳区某路2号', '现结', 'ACTIVE');
INSERT INTO `supplier` VALUES (3, '3', '', '', '', '', '', 'ACTIVE');

-- ----------------------------
-- Table structure for supply
-- ----------------------------
DROP TABLE IF EXISTS `supply`;
CREATE TABLE `supply`  (
  `supplier_id` bigint NOT NULL,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `supply_price` decimal(10, 2) NOT NULL,
  `lead_time_days` int NULL DEFAULT NULL,
  `is_primary` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`supplier_id`, `book_id`) USING BTREE,
  INDEX `fk_supply_book`(`book_id` ASC) USING BTREE,
  CONSTRAINT `fk_supply_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_supply_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of supply
-- ----------------------------
INSERT INTO `supply` VALUES (1, 'B001', 40.00, 5, 1);
INSERT INTO `supply` VALUES (1, 'B002', 80.00, 7, 1);
INSERT INTO `supply` VALUES (2, 'B001', 20.00, 4, 0);
INSERT INTO `supply` VALUES (2, 'B003', 60.00, 10, 1);

SET FOREIGN_KEY_CHECKS = 1;

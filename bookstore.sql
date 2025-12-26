/*
 Navicat Premium Dump SQL

 Source Server         : BookStore
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : bookstore1

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 26/12/2025 14:04:36
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for book_inquiry_request
-- ----------------------------
DROP TABLE IF EXISTS `book_inquiry_request`;
CREATE TABLE `book_inquiry_request`  (
  `inquiry_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `book_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `book_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `publisher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `isbn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `customer_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `inquiry_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('PENDING','QUOTED','REJECTED','ACCEPTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `admin_reply` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `quoted_price` decimal(10, 2) NULL DEFAULT NULL,
  `reply_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`inquiry_id`) USING BTREE,
  INDEX `idx_inquiry_customer`(`customer_id` ASC) USING BTREE,
  INDEX `idx_inquiry_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_inquiry_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
  INDEX `idx_notif_customer`(`customer_id` ASC) USING BTREE,
  CONSTRAINT `fk_cn_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_cn_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for keyword
-- ----------------------------
DROP TABLE IF EXISTS `keyword`;
CREATE TABLE `keyword`  (
  `keyword_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`keyword_id`) USING BTREE,
  UNIQUE INDEX `keyword_text`(`keyword_text` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
  UNIQUE INDEX `uk_oos_book_status`(`book_id` ASC, `status` ASC) USING BTREE,
  INDEX `fk_oos_customer`(`related_customer_id` ASC) USING BTREE,
  CONSTRAINT `fk_oos_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_oos_customer` FOREIGN KEY (`related_customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `book_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cart_item_id`) USING BTREE,
  UNIQUE INDEX `uk_customer_book`(`customer_id` ASC, `book_id` ASC) USING BTREE,
  INDEX `book_id`(`book_id` ASC) USING BTREE,
  CONSTRAINT `shopping_cart_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `shopping_cart_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View structure for v_series_overview
-- ----------------------------
DROP VIEW IF EXISTS `v_series_overview`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_series_overview` AS select `p`.`book_id` AS `series_id`,`p`.`title` AS `series_title`,`p`.`price` AS `series_price`,`p`.`publisher` AS `series_publisher`,(select count(0) from `book` where (`book`.`parent_book_id` = `p`.`book_id`)) AS `volume_count`,group_concat(`c`.`book_id` order by `c`.`book_id` ASC separator ', ') AS `child_book_ids`,group_concat(`c`.`title` order by `c`.`book_id` ASC separator ' | ') AS `child_titles`,sum(coalesce(`i`.`quantity`,0)) AS `total_child_stock` from ((`book` `p` left join `book` `c` on((`c`.`parent_book_id` = `p`.`book_id`))) left join `inventory` `i` on((`i`.`book_id` = `c`.`book_id`))) where (`p`.`series_flag` = 1) group by `p`.`book_id`,`p`.`title`,`p`.`price`,`p`.`publisher`;

SET FOREIGN_KEY_CHECKS = 1;

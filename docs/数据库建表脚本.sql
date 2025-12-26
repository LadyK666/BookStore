-- ===============================================
-- 网上书店管理系统 - MySQL 建库与核心表结构脚本
-- 你可以在 Navicat / MySQL 命令行中整段执行
-- ===============================================

-- 1. 创建数据库（建议名：bookstore）
CREATE DATABASE IF NOT EXISTS bookstore
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE bookstore;

-- 2. 信用等级表 credit_level
CREATE TABLE IF NOT EXISTS credit_level (
    level_id         INT PRIMARY KEY AUTO_INCREMENT,
    level_name       VARCHAR(50) NOT NULL,
    discount_rate    DECIMAL(5,4) NOT NULL,   -- 0.90 表示 9 折（10% 折扣）
    allow_overdraft  TINYINT(1) NOT NULL,     -- 0 否  1 是
    overdraft_limit  DECIMAL(12,2) NOT NULL,  -- 不允许透支可设为 0；无限额可用 -1 表示
    upgrade_condition VARCHAR(255) NULL
);

-- 预置五级信用规则
INSERT INTO credit_level (level_name, discount_rate, allow_overdraft, overdraft_limit, upgrade_condition) VALUES
('一级', 0.90, 0, 0.00, NULL),
('二级', 0.85, 0, 0.00, NULL),
('三级', 0.85, 1, 500.00, '可选：累计消费/余额达到某值'),
('四级', 0.80, 1, 2000.00, '可选：累计消费/余额达到某值'),
('五级', 0.75, 1, -1.00, '可选：累计消费/余额达到某值')
ON DUPLICATE KEY UPDATE
    discount_rate = VALUES(discount_rate),
    allow_overdraft = VALUES(allow_overdraft),
    overdraft_limit = VALUES(overdraft_limit),
    upgrade_condition = VALUES(upgrade_condition);

-- 3. 客户表 customer
CREATE TABLE IF NOT EXISTS customer (
    customer_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    username          VARCHAR(50) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    real_name         VARCHAR(100) NOT NULL,
    mobile_phone      VARCHAR(20),
    email             VARCHAR(100),
    account_balance   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_consumption DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    registration_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_status    ENUM('NORMAL','FROZEN') NOT NULL DEFAULT 'NORMAL',
    credit_level_id   INT NOT NULL,
    CONSTRAINT fk_customer_credit_level
        FOREIGN KEY (credit_level_id) REFERENCES credit_level(level_id)
);

-- 4. 书目表 book
CREATE TABLE IF NOT EXISTS book (
    book_id         VARCHAR(32) PRIMARY KEY,       -- 可用内部编码或 ISBN
    isbn            VARCHAR(32) UNIQUE,
    title           VARCHAR(255) NOT NULL,
    publisher       VARCHAR(255) NOT NULL,
    publish_date    DATE,
    edition         VARCHAR(50),
    price           DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    cover_image_url VARCHAR(255),
    catalog         TEXT,
    series_flag     TINYINT(1) NOT NULL DEFAULT 0,
    parent_book_id  VARCHAR(32),
    status          ENUM('AVAILABLE','UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_book_parent
        FOREIGN KEY (parent_book_id) REFERENCES book(book_id)
);

-- 5. 库存表 inventory
CREATE TABLE IF NOT EXISTS inventory (
    book_id       VARCHAR(32) PRIMARY KEY,
    quantity      INT NOT NULL DEFAULT 0,
    safety_stock  INT NOT NULL DEFAULT 0,
    location_code VARCHAR(50),
    CONSTRAINT fk_inventory_book
        FOREIGN KEY (book_id) REFERENCES book(book_id)
);

-- 6. 订单主表 sales_order
CREATE TABLE IF NOT EXISTS sales_order (
    order_id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id            BIGINT NOT NULL,
    order_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    order_status           ENUM('PENDING_PAYMENT','OUT_OF_STOCK_PENDING','PENDING_SHIPMENT','SHIPPED','COMPLETED','CANCELLED')
                           NOT NULL DEFAULT 'PENDING_PAYMENT',
    goods_amount           DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_rate_snapshot DECIMAL(5,4) NOT NULL,
    payable_amount         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    shipping_address_snapshot VARCHAR(500) NOT NULL,
    payment_time           DATETIME NULL,
    delivery_time          DATETIME NULL,
    customer_note          VARCHAR(500) NULL,
    CONSTRAINT fk_order_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- 7. 订单明细表 sales_order_item
CREATE TABLE IF NOT EXISTS sales_order_item (
    order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id      BIGINT NOT NULL,
    book_id       VARCHAR(32) NOT NULL,
    quantity      INT NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,      -- 成交单价（已经折扣）
    sub_amount    DECIMAL(12,2) NOT NULL,      -- quantity * unit_price
    item_status   ENUM('ORDERED','SHIPPED','COMPLETED') NOT NULL DEFAULT 'ORDERED',
    CONSTRAINT fk_item_order
        FOREIGN KEY (order_id) REFERENCES sales_order(order_id),
    CONSTRAINT fk_item_book
        FOREIGN KEY (book_id) REFERENCES book(book_id)
);

CREATE INDEX IF NOT EXISTS idx_sales_order_customer ON sales_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_order_item_order ON sales_order_item(order_id);

-- 8. 供应商与供货关系表

-- 8.1 供应商信息表 supplier
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_name  VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    phone          VARCHAR(50),
    email          VARCHAR(100),
    address        VARCHAR(255),
    payment_terms  VARCHAR(100),
    cooperation_status ENUM('ACTIVE','SUSPENDED','TERMINATED') NOT NULL DEFAULT 'ACTIVE'
);

-- 8.2 供货关系表 supply（多对多：供应商 - 书目）
CREATE TABLE IF NOT EXISTS supply (
    supplier_id   BIGINT NOT NULL,
    book_id       VARCHAR(32) NOT NULL,
    supply_price  DECIMAL(10,2) NOT NULL,
    lead_time_days INT NULL,
    is_primary    TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (supplier_id, book_id),
    CONSTRAINT fk_supply_supplier
        FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    CONSTRAINT fk_supply_book
        FOREIGN KEY (book_id) REFERENCES book(book_id)
);

-- 8.3 供应商测试数据
INSERT INTO supplier (supplier_name, contact_person, phone, email, address, payment_terms, cooperation_status)
VALUES
('新华图书批发中心', '王经理', '010-88888888', 'boss@xinhua.com', '北京市海淀区某路1号', '月结30天', 'ACTIVE'),
('高校教材供应站', '李老师', '010-66666666', 'textbook@supplier.com', '北京市朝阳区某路2号', '现结', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    contact_person = VALUES(contact_person),
    phone          = VALUES(phone),
    email          = VALUES(email),
    address        = VALUES(address),
    payment_terms  = VALUES(payment_terms),
    cooperation_status = VALUES(cooperation_status);

-- 获取插入或已存在的供应商ID（实际开发中可通过 SELECT 查询，这里在文档中只给出示例值）
-- 假定：新华图书批发中心 的 supplier_id = 1，高校教材供应站 的 supplier_id = 2

INSERT INTO supply (supplier_id, book_id, supply_price, lead_time_days, is_primary) VALUES
(1, 'B001', 40.00, 5, 1),
(1, 'B002', 80.00, 7, 1),
(2, 'B003', 60.00, 10, 1)
ON DUPLICATE KEY UPDATE
    supply_price   = VALUES(supply_price),
    lead_time_days = VALUES(lead_time_days),
    is_primary     = VALUES(is_primary);

-- ===============================================
-- 9. 插入一些测试数据，便于后续 JDBC 开发联调
-- ===============================================

-- 9.1 测试图书与库存
INSERT INTO book (book_id, isbn, title, publisher, publish_date, edition, price, status)
VALUES
('B001', '9787302290209', '数据库系统概论', '高等教育出版社', '2014-08-01', '第5版', 59.00, 'AVAILABLE'),
('B002', '9787111213826', 'Java 编程思想', '机械工业出版社', '2007-06-01', '第4版', 108.00, 'AVAILABLE'),
('B003', '9787121210979', 'Head First Java', '人民邮电出版社', '2012-01-01', '第2版', 79.00, 'AVAILABLE')
ON DUPLICATE KEY UPDATE
    title     = VALUES(title),
    publisher = VALUES(publisher),
    price     = VALUES(price),
    status    = VALUES(status);

INSERT INTO inventory (book_id, quantity, safety_stock, location_code) VALUES
('B001', 50, 5, 'A-01-01'),
('B002', 20, 3, 'A-01-02'),
('B003', 15, 2, 'A-01-03')
ON DUPLICATE KEY UPDATE
    quantity     = VALUES(quantity),
    safety_stock = VALUES(safety_stock),
    location_code = VALUES(location_code);

-- 9.2 测试客户
INSERT INTO customer (username, password_hash, real_name, mobile_phone, email, account_balance, total_consumption, credit_level_id)
VALUES
('zhangsan', 'test-hash-zhangsan', '张三', '13800000001', 'zhangsan@example.com', 500.00, 0.00, 3),
('lisi',     'test-hash-lisi',     '李四', '13800000002', 'lisi@example.com',     1000.00, 2000.00, 4)
ON DUPLICATE KEY UPDATE
    real_name        = VALUES(real_name),
    mobile_phone     = VALUES(mobile_phone),
    email            = VALUES(email),
    account_balance  = VALUES(account_balance),
    total_consumption = VALUES(total_consumption),
    credit_level_id  = VALUES(credit_level_id);

-- 9.3 客户地址表 customer_address
CREATE TABLE IF NOT EXISTS customer_address (
    address_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id  BIGINT NOT NULL,
    receiver     VARCHAR(100) NOT NULL,
    phone        VARCHAR(20),
    province     VARCHAR(50),
    city         VARCHAR(50),
    district     VARCHAR(50),
    detail       VARCHAR(255) NOT NULL,
    is_default   TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_address_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE INDEX IF NOT EXISTS idx_address_customer ON customer_address(customer_id);

-- ===============================================
-- 10. 作者与关键字相关表（书目信息扩展）
-- ===============================================

-- 10.1 作者表 author
CREATE TABLE IF NOT EXISTS author (
    author_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_name VARCHAR(100) NOT NULL,
    nationality VARCHAR(100),
    biography   TEXT
);

-- 10.2 书目-作者关系表 book_author
CREATE TABLE IF NOT EXISTS book_author (
    book_id      VARCHAR(32) NOT NULL,
    author_id    BIGINT NOT NULL,
    author_order TINYINT NOT NULL,
    PRIMARY KEY (book_id, author_order),
    CONSTRAINT fk_book_author_book
        FOREIGN KEY (book_id) REFERENCES book(book_id),
    CONSTRAINT fk_book_author_author
        FOREIGN KEY (author_id) REFERENCES author(author_id)
);

-- 10.3 关键字表 keyword
CREATE TABLE IF NOT EXISTS keyword (
    keyword_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword_text VARCHAR(100) NOT NULL UNIQUE
);

-- 10.4 书目-关键字关系表 book_keyword
CREATE TABLE IF NOT EXISTS book_keyword (
    book_id    VARCHAR(32) NOT NULL,
    keyword_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, keyword_id),
    CONSTRAINT fk_book_keyword_book
        FOREIGN KEY (book_id) REFERENCES book(book_id),
    CONSTRAINT fk_book_keyword_keyword
        FOREIGN KEY (keyword_id) REFERENCES keyword(keyword_id)
);

-- ===============================================
-- 11. 缺书记录与采购管理相关表
-- ===============================================

-- 11.1 缺书记录表 out_of_stock_record
CREATE TABLE IF NOT EXISTS out_of_stock_record (
    record_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id             VARCHAR(32) NOT NULL,
    required_quantity   INT NOT NULL,
    record_date         DATE NOT NULL,
    source              ENUM('MANUAL','LOW_STOCK','ORDER_EXCEED','CUSTOMER_REQUEST') NOT NULL DEFAULT 'MANUAL',
    related_customer_id BIGINT NULL,
    status              ENUM('PENDING','PURCHASING','COMPLETED') NOT NULL DEFAULT 'PENDING',
    priority            INT NULL,
    CONSTRAINT fk_oos_book
        FOREIGN KEY (book_id) REFERENCES book(book_id),
    CONSTRAINT fk_oos_customer
        FOREIGN KEY (related_customer_id) REFERENCES customer(customer_id)
);

-- 11.2 采购单主表 purchase_order
CREATE TABLE IF NOT EXISTS purchase_order (
    purchase_order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id       BIGINT NOT NULL,
    create_date       DATE NOT NULL,
    expected_date     DATE NULL,
    buyer             VARCHAR(100),
    estimated_amount  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status            ENUM('DRAFT','ISSUED','PARTIAL_RECEIVED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT fk_po_supplier
        FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
);

-- 11.3 采购明细表 purchase_order_item
CREATE TABLE IF NOT EXISTS purchase_order_item (
    purchase_order_id       BIGINT NOT NULL,
    book_id                 VARCHAR(32) NOT NULL,
    purchase_quantity       INT NOT NULL,
    purchase_price          DECIMAL(10,2) NOT NULL,
    related_out_of_stock_id BIGINT NULL,
    PRIMARY KEY (purchase_order_id, book_id),
    CONSTRAINT fk_poi_order
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id),
    CONSTRAINT fk_poi_book
        FOREIGN KEY (book_id) REFERENCES book(book_id),
    CONSTRAINT fk_poi_oos
        FOREIGN KEY (related_out_of_stock_id) REFERENCES out_of_stock_record(record_id)
);

-- ===============================================
-- 12. 书籍询价请求表（当书库中未找到时顾客提交的询价）
-- ===============================================
CREATE TABLE IF NOT EXISTS book_inquiry_request (
    inquiry_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id     BIGINT NOT NULL,
    book_title      VARCHAR(255) NOT NULL,
    book_author     VARCHAR(255),
    publisher       VARCHAR(255),
    isbn            VARCHAR(32),
    quantity        INT NOT NULL DEFAULT 1,
    customer_note   VARCHAR(500),
    inquiry_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          ENUM('PENDING','QUOTED','REJECTED','ACCEPTED') NOT NULL DEFAULT 'PENDING',
    admin_reply     VARCHAR(500),
    quoted_price    DECIMAL(10,2),
    reply_time      DATETIME,
    CONSTRAINT fk_inquiry_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE INDEX IF NOT EXISTS idx_inquiry_customer ON book_inquiry_request(customer_id);
CREATE INDEX IF NOT EXISTS idx_inquiry_status ON book_inquiry_request(status);
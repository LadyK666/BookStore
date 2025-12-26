# 在线书店管理系统

一个基于 Spring Boot + React + MySQL 的在线书店管理系统，支持管理员和客户两种角色，提供完整的图书管理、订单处理、库存管理等功能。

## 📁 目录结构

```
BookStore/
├── src/                          # 后端源代码
│   └── main/
│       ├── java/
│       │   └── com/bookstore/
│       │       ├── dao/          # 数据访问层（18个DAO类）
│       │       ├── model/        # 实体类（19个Model类）
│       │       ├── service/      # 业务逻辑层（OrderService, PurchaseService, ShipmentService）
│       │       ├── util/         # 工具类（DBUtil）
│       │       └── web/
│       │           ├── controller/  # REST API控制器（15个Controller）
│       │           └── WebApplication.java  # Spring Boot启动类
│       └── resources/
│           └── db.properties     # 数据库配置文件
├── web-frontend/                  # 前端源代码
│   ├── src/
│   │   ├── api/                  # API请求封装
│   │   ├── pages/                # 页面组件
│   │   │   ├── admin/           # 管理员页面
│   │   │   ├── customer/        # 客户页面
│   │   │   └── LoginPage.tsx    # 登录页面
│   │   └── styles/               # 样式文件
│   ├── package.json             # 前端依赖配置
│   └── vite.config.ts           # Vite构建配置
├── static/                       # 静态资源目录
│   └── images/                   # 图片资源（书籍封面等）
├── bookstore.sql                 # 数据库初始化脚本
├── pom.xml                       # Maven项目配置
└── README.md                     # 项目说明文档
```

## 🛠️ 环境要求

### 后端环境
- **JDK**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **Spring Boot**: 3.x
- **MySQL**: 8.0 或更高版本

### 前端环境
- **Node.js**: 16.x 或更高版本
- **npm**: 8.x 或更高版本

## 🚀 运行方式

### 1. 数据库初始化

使用 MySQL 运行 `bookstore.sql` 文件来初始化数据库：

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS bookstore1 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 使用数据库
USE bookstore1;

# 执行初始化脚本
SOURCE /path/to/bookstore.sql;
```

或者直接使用命令行：

```bash
mysql -u root -p bookstore1 < bookstore.sql
```

### 2. 修改数据库配置

根据本地 MySQL 设置，修改 `src/main/resources/db.properties` 文件：

```properties
db.url=jdbc:mysql://localhost:3306/bookstore1?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
db.username=root
db.password=你的密码
db.maximumPoolSize=10
```

**注意**：请将 `db.username` 和 `db.password` 修改为你本地 MySQL 的用户名和密码。

### 3. 启动后端服务

在项目根目录下执行：

```bash
# 清理并编译项目
mvn clean compile

# 打包项目（跳过测试）
mvn package -DskipTests

# 运行后端服务
java -jar target/online-bookstore-1.0.0-SNAPSHOT.jar
```

后端服务默认运行在 `http://localhost:8080`

### 4. 启动前端服务

在 `web-frontend` 目录下执行：

```bash
# 进入前端目录
cd web-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务默认运行在 `http://localhost:5173`

## 📋 实现功能

### 管理员功能

#### 书目管理
- ✅ 添加、编辑、删除书目
- ✅ 支持普通书籍、丛书、子书三种类型
- ✅ 维护书籍的作者、关键字、供货关系
- ✅ 书籍封面图片支持（本地图片和外部URL）

#### 库存管理
- ✅ 库存概览和实时查询
- ✅ 安全库存设置和预警（库存低于安全库存时显示红色）
- ✅ 快速库存调整功能
- ✅ 缺货记录管理

#### 订单管理
- ✅ 查看所有客户订单
- ✅ 订单状态管理（待付款、待发货、配送中、已完成等）
- ✅ 发货单管理
- ✅ 订单详情查看

#### 采购管理
- ✅ 缺书记录管理
- ✅ 采购单创建和管理
- ✅ 供应商管理（添加、编辑、删除供应商）
- ✅ 供应商供货清单管理

#### 客户管理
- ✅ 客户列表查看
- ✅ 客户信用等级调整
- ✅ 客户账户状态管理

#### 询价管理
- ✅ 客户询价请求处理
- ✅ 报价和回复功能

### 客户功能

#### 图书浏览
- ✅ 图书列表浏览
- ✅ 高级搜索功能（支持多条件组合搜索）
- ✅ 搜索结果返回初始界面功能

#### 购物车
- ✅ 添加商品到购物车
- ✅ 购物车商品管理（修改数量、删除）
- ✅ 购物车结算

#### 订单管理
- ✅ 下单功能
- ✅ 订单列表查看（支持按状态筛选）
- ✅ 订单详情查看
- ✅ 订单支付功能
- ✅ 订单收货确认
- ✅ 订单取消功能
- ✅ **先发货后付款**特权支持（高信用等级客户）

#### 个人中心
- ✅ 个人信息查看和修改
- ✅ 收货地址管理
- ✅ 账户余额查看

#### 其他功能
- ✅ 图书询价功能
- ✅ 缺货登记功能
- ✅ 通知消息查看

### 核心业务逻辑

#### 信用等级系统
- 支持5个信用等级，不同等级享受不同折扣和权限
- 高信用等级客户支持"先发货后付款"特权
- 信用等级可手动调整

#### 订单状态流转
- **PENDING_PAYMENT** → **PENDING_SHIPMENT** → **DELIVERING** → **COMPLETED**
- 支持先发货后付款的特殊流程
- 支持分次发货和部分收货

#### 库存管理
- 实时库存跟踪
- 安全库存预警机制
- 自动缺货记录生成

#### 丛书系统
- 支持丛书（套装）和子书类型
- 丛书删除时自动删除所有子书
- 子书不能单独删除，需删除父丛书

## 🔧 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **连接池**: HikariCP
- **构建工具**: Maven

### 前端
- **框架**: React 18
- **UI组件库**: Ant Design 5
- **路由**: React Router 6
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **开发语言**: TypeScript

## 📝 注意事项

1. **数据库配置**：首次运行前必须修改 `db.properties` 中的数据库用户名和密码
2. **端口冲突**：确保 8080（后端）和 5173（前端）端口未被占用
3. **MySQL版本**：建议使用 MySQL 8.0+，已配置 `allowPublicKeyRetrieval=true` 以解决认证问题
4. **图片资源**：本地图片需放置在 `static/images/` 目录下，访问路径为 `/images/文件名`
5. **丛书删除**：删除丛书时会级联删除所有子书及相关数据，请谨慎操作

## 📄 许可证

本项目为课程设计项目，仅供学习使用。

## 👥 开发团队

本项目为数据库课程设计项目。

---

**最后更新**: 2025-12-25


package com.bookstore.web.controller;

import com.bookstore.dao.CustomerDao;
import com.bookstore.model.Customer;
import com.bookstore.web.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证相关接口：
 * - /api/auth/login：复刻 LoginView.handleLogin 的逻辑
 * - /api/auth/register：复刻 LoginView.showRegisterDialog 的注册逻辑（简化部分可选字段）
 *
 * 为了保持“后端不受影响”，我们直接重用 CustomerDao / Customer 模型。
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final CustomerDao customerDao = new CustomerDao();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword();
        String userType = req.getUserType();

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResp("请输入用户名和密码"));
        }

        if ("ADMIN".equalsIgnoreCase(userType)) {
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                AdminLoginResp resp = new AdminLoginResp();
                resp.setAdminName(username);
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResp("管理员账号或密码错误"));
            }
        } else {
            try {
                Customer customer = customerDao.findByUsername(username);
                if (customer == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ErrorResp("用户不存在"));
                } else if (!password.equals(customer.getPasswordHash())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ErrorResp("密码错误"));
                } else if ("FROZEN".equals(customer.getAccountStatus())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ErrorResp("账户已被冻结，请联系管理员"));
                }
                CustomerLoginResp resp = new CustomerLoginResp();
                resp.setCustomerId(customer.getCustomerId());
                resp.setCustomerName(customer.getRealName());
                return ResponseEntity.ok(resp);
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResp("登录失败：" + ex.getMessage()));
            }
        }
    }

    /**
     * 顾客注册：复刻 LoginView.showRegisterDialog 的插入逻辑（保留必填校验和重名检查）。
     * 为简化前端交互，这里只强制用户名/密码，其他字段可以之后在“个人信息”中维护。
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        String username = req.getUsername() == null ? "" : req.getUsername().trim();
        String password = req.getPassword() == null ? "" : req.getPassword();
        String realName = req.getRealName() == null ? "" : req.getRealName().trim();
        String phone = req.getMobilePhone();
        String email = req.getEmail();

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResp("用户名和密码不能为空"));
        }

        try {
            if (customerDao.findByUsername(username) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResp("用户名已存在"));
            }
            Customer c = new Customer();
            c.setUsername(username);
            c.setPasswordHash(password); // 与 JavaFX 一致，简化处理
            c.setRealName(realName.isEmpty() ? username : realName);
            c.setMobilePhone(phone);
            c.setEmail(email);
            c.setCreditLevelId(1);
            int n = customerDao.insert(c);
            if (n > 0) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResp("注册失败"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResp("注册失败：" + ex.getMessage()));
        }
    }

    // ======== 内部简单 DTO / 响应对象（为了不污染现有 model 层） ========

    public static class ErrorResp {
        private String message;

        public ErrorResp(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class CustomerLoginResp {
        private Long customerId;
        private String customerName;

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
    }

    public static class AdminLoginResp {
        private String adminName;

        public String getAdminName() {
            return adminName;
        }

        public void setAdminName(String adminName) {
            this.adminName = adminName;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private String mobilePhone;
        private String email;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}



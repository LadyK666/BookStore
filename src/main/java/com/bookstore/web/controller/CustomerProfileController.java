package com.bookstore.web.controller;

import com.bookstore.dao.CreditLevelDao;
import com.bookstore.dao.CustomerDao;
import com.bookstore.model.CreditLevel;
import com.bookstore.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * 顾客端-个人信息/信用等级接口。
 * 逻辑对应 CustomerView.loadCustomerInfo 和 getCreditPrivilegeText。
 */
@RestController
@RequestMapping("/api/customer")
@CrossOrigin
public class CustomerProfileController {

    private final CustomerDao customerDao = new CustomerDao();
    private final CreditLevelDao creditLevelDao = new CreditLevelDao();

    @GetMapping("/{customerId}/summary")
    public ResponseEntity<?> summary(@PathVariable("customerId") long customerId) throws SQLException {
        Customer c = customerDao.findById(customerId);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        CreditLevel level = creditLevelDao.findById(c.getCreditLevelId());
        ProfileResp resp = toProfileResp(c, level);
        return ResponseEntity.ok(resp);
    }

    private String buildPrivilegeText(CreditLevel level) {
        int levelId = level.getLevelId();
        StringBuilder sb = new StringBuilder();
        sb.append("权限：");
        if (levelId <= 2) {
            sb.append("不能透支，必须先付款后发货");
        } else if (levelId <= 4) {
            sb.append("可透支");
            if (level.getOverdraftLimit() != null
                    && level.getOverdraftLimit().compareTo(BigDecimal.valueOf(-1)) != 0) {
                sb.append("（额度：¥").append(level.getOverdraftLimit()).append("）");
            }
            sb.append("，可先发货后付款");
        } else {
            sb.append("可无限透支，可先发货后付款");
        }
        return sb.toString();
    }

    private ProfileResp toProfileResp(Customer c, CreditLevel level) {
        ProfileResp resp = new ProfileResp();
        resp.setCustomerId(c.getCustomerId());
        resp.setUsername(c.getUsername());
        resp.setRealName(c.getRealName());
        resp.setMobilePhone(c.getMobilePhone());
        resp.setEmail(c.getEmail());
        resp.setAccountBalance(c.getAccountBalance());
        if (level != null) {
            resp.setCreditLevelId(level.getLevelId());
            resp.setCreditLevelName(level.getLevelName());
            resp.setDiscountRate(level.getDiscountRate());
            resp.setPrivilegeText(buildPrivilegeText(level));
        }
        return resp;
    }

    /**
     * 账户充值：逻辑对应 CustomerView.showRechargeDialog() 中的充值行为。
     */
    @PostMapping("/{customerId}/recharge")
    public ResponseEntity<?> recharge(@PathVariable("customerId") long customerId,
                                      @RequestBody RechargeReq req) {
        try {
            if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("充值金额必须大于0"));
            }
            Customer c = customerDao.findById(customerId);
            if (c == null) {
                return ResponseEntity.notFound().build();
            }
            BigDecimal newBalance = c.getAccountBalance().add(req.getAmount());
            customerDao.updateAccountBalance(customerId, newBalance);
            c.setAccountBalance(newBalance);
            CreditLevel level = creditLevelDao.findById(c.getCreditLevelId());
            return ResponseEntity.ok(toProfileResp(c, level));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp("充值失败：" + e.getMessage()));
        }
    }

    /**
     * 修改个人信息：逻辑对应 CustomerView.showEditCustomerInfoDialog()。
     */
    @PutMapping("/{customerId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable("customerId") long customerId,
                                           @RequestBody UpdateProfileReq req) {
        try {
            String realName = req.getRealName() == null ? "" : req.getRealName().trim();
            String mobile = req.getMobilePhone();
            String email = req.getEmail();

            if (realName.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("真实姓名不能为空"));
            }
            if (email != null && !email.isEmpty() && !email.contains("@")) {
                return ResponseEntity.badRequest().body(new ErrorResp("邮箱格式不正确"));
            }

            int n = customerDao.updateCustomerInfo(customerId, realName,
                    (mobile == null || mobile.isEmpty()) ? null : mobile,
                    (email == null || email.isEmpty()) ? null : email);
            if (n <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("更新失败"));
            }
            Customer c = customerDao.findById(customerId);
            CreditLevel level = creditLevelDao.findById(c.getCreditLevelId());
            return ResponseEntity.ok(toProfileResp(c, level));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp("更新失败：" + e.getMessage()));
        }
    }

    public static class ProfileResp {
        private Long customerId;
        private String username;
        private String realName;
        private String mobilePhone;
        private String email;
        private BigDecimal accountBalance;
        private Integer creditLevelId;
        private String creditLevelName;
        private BigDecimal discountRate;
        private String privilegeText;

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public BigDecimal getAccountBalance() {
            return accountBalance;
        }

        public void setAccountBalance(BigDecimal accountBalance) {
            this.accountBalance = accountBalance;
        }

        public Integer getCreditLevelId() {
            return creditLevelId;
        }

        public void setCreditLevelId(Integer creditLevelId) {
            this.creditLevelId = creditLevelId;
        }

        public String getCreditLevelName() {
            return creditLevelName;
        }

        public void setCreditLevelName(String creditLevelName) {
            this.creditLevelName = creditLevelName;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }

        public void setDiscountRate(BigDecimal discountRate) {
            this.discountRate = discountRate;
        }

        public String getPrivilegeText() {
            return privilegeText;
        }

        public void setPrivilegeText(String privilegeText) {
            this.privilegeText = privilegeText;
        }
    }

    public static class RechargeReq {
        private BigDecimal amount;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class UpdateProfileReq {
        private String realName;
        private String mobilePhone;
        private String email;

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
}



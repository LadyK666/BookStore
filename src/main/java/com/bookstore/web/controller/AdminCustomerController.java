package com.bookstore.web.controller;

import com.bookstore.dao.CustomerDao;
import com.bookstore.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * 管理员端 - 客户管理相关接口。
 *
 * 对应 AdminView.showCustomerManagement / showCreditDialog：
 * - 查询全部客户列表；
 * - 调整客户信用等级。
 */
@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin
public class AdminCustomerController {

    private final CustomerDao customerDao = new CustomerDao();

    /**
     * 查询全部客户。
     */
    @GetMapping
    public ResponseEntity<List<Customer>> listCustomers() throws SQLException {
        return ResponseEntity.ok(customerDao.findAll());
    }

    /**
     * 调整客户信用等级。
     */
    @PostMapping("/{customerId}/credit-level")
    public ResponseEntity<?> updateCreditLevel(@PathVariable("customerId") long customerId,
                                               @RequestBody UpdateCreditReq req) {
        try {
            if (req == null || req.getCreditLevelId() == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("信用等级不能为空"));
            }
            int level = req.getCreditLevelId();
            if (level < 1 || level > 5) {
                return ResponseEntity.badRequest().body(new ErrorResp("信用等级必须在 1~5 之间"));
            }
            customerDao.updateCreditLevel(customerId, level);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    public static class UpdateCreditReq {
        private Integer creditLevelId;

        public Integer getCreditLevelId() {
            return creditLevelId;
        }

        public void setCreditLevelId(Integer creditLevelId) {
            this.creditLevelId = creditLevelId;
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



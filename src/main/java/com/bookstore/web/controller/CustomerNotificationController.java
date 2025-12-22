package com.bookstore.web.controller;

import com.bookstore.dao.CustomerNotificationDao;
import com.bookstore.model.CustomerNotification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * 顾客端 - 消息通知相关接口。
 * 逻辑对应 CustomerView.showOutOfStockNotificationList():
 * - 按顾客 ID 查询所有历史通知，按时间倒序展示。
 */
@RestController
@RequestMapping("/api/customer")
@CrossOrigin
public class CustomerNotificationController {

    private final CustomerNotificationDao notificationDao = new CustomerNotificationDao();

    /**
     * 获取指定顾客的全部通知（按时间倒序）。
     */
    @GetMapping("/{customerId}/notifications")
    public ResponseEntity<List<CustomerNotification>> listNotifications(
            @PathVariable("customerId") long customerId) throws SQLException {
        List<CustomerNotification> list = notificationDao.findByCustomerId(customerId);
        return ResponseEntity.ok(list);
    }
}



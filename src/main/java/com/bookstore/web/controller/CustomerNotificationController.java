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

    /**
     * 标记单条通知为已读。
     */
    @PostMapping("/{customerId}/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("customerId") long customerId,
            @PathVariable("notificationId") long notificationId) throws SQLException {
        notificationDao.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 标记所有通知为已读。
     */
    @PostMapping("/{customerId}/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable("customerId") long customerId) throws SQLException {
        notificationDao.markAllAsRead(customerId);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除单条通知。
     */
    @DeleteMapping("/{customerId}/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable("customerId") long customerId,
            @PathVariable("notificationId") long notificationId) throws SQLException {
        notificationDao.delete(notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除所有通知。
     */
    @DeleteMapping("/{customerId}/notifications")
    public ResponseEntity<Void> deleteAllNotifications(@PathVariable("customerId") long customerId)
            throws SQLException {
        notificationDao.deleteAllByCustomer(customerId);
        return ResponseEntity.ok().build();
    }
}

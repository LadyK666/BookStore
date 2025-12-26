package com.bookstore.dao;

import com.bookstore.model.CustomerNotification;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户通知 DAO，对应表：customer_notification。
 */
public class CustomerNotificationDao {

    public long insert(CustomerNotification n) throws SQLException {
        String sql = "INSERT INTO customer_notification " +
                "(customer_id, order_id, type, title, content, created_time, read_flag) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, n.getCustomerId());
            if (n.getOrderId() != null) {
                ps.setLong(2, n.getOrderId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, n.getType());
            ps.setString(4, n.getTitle());
            ps.setString(5, n.getContent());
            ps.setTimestamp(6, Timestamp.valueOf(
                    n.getCreatedTime() != null ? n.getCreatedTime() : LocalDateTime.now()));
            ps.setBoolean(7, n.isReadFlag());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    public List<CustomerNotification> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT notification_id, customer_id, order_id, type, title, content, created_time, read_flag " +
                "FROM customer_notification WHERE customer_id = ? ORDER BY created_time DESC, notification_id DESC";
        List<CustomerNotification> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private CustomerNotification mapRow(ResultSet rs) throws SQLException {
        CustomerNotification n = new CustomerNotification();
        n.setNotificationId(rs.getLong("notification_id"));
        n.setCustomerId(rs.getLong("customer_id"));
        long oid = rs.getLong("order_id");
        if (!rs.wasNull()) {
            n.setOrderId(oid);
        }
        n.setType(rs.getString("type"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        Timestamp ts = rs.getTimestamp("created_time");
        if (ts != null) {
            n.setCreatedTime(ts.toLocalDateTime());
        }
        n.setReadFlag(rs.getBoolean("read_flag"));
        return n;
    }

    public void markAsRead(long notificationId) throws SQLException {
        String sql = "UPDATE customer_notification SET read_flag = 1 WHERE notification_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.executeUpdate();
        }
    }

    public void markAllAsRead(long customerId) throws SQLException {
        String sql = "UPDATE customer_notification SET read_flag = 1 WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.executeUpdate();
        }
    }

    public void delete(long notificationId) throws SQLException {
        String sql = "DELETE FROM customer_notification WHERE notification_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.executeUpdate();
        }
    }

    public void deleteAllByCustomer(long customerId) throws SQLException {
        String sql = "DELETE FROM customer_notification WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.executeUpdate();
        }
    }
}

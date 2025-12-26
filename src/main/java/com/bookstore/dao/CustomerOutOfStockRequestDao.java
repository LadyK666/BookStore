package com.bookstore.dao;

import com.bookstore.model.CustomerOutOfStockRequest;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 顾客缺书登记 DAO，对应表 customer_out_of_stock_request。
 */
public class CustomerOutOfStockRequestDao {

    /**
     * 插入一条顾客缺书登记记录。
     */
    public long insert(CustomerOutOfStockRequest req) throws SQLException {
        String sql = "INSERT INTO customer_out_of_stock_request " +
                "(order_id, customer_id, book_id, requested_qty, customer_note, is_paid, processed_status, related_record_id, customer_notified, created_at, processed_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, req.getOrderId());
            ps.setLong(2, req.getCustomerId());
            ps.setString(3, req.getBookId());
            ps.setInt(4, req.getRequestedQty());
            ps.setString(5, req.getCustomerNote());
            ps.setBoolean(6, req.isPaid());
            ps.setString(7, req.getProcessedStatus());
            if (req.getRelatedRecordId() != null) {
                ps.setLong(8, req.getRelatedRecordId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            ps.setBoolean(9, req.isCustomerNotified());
            LocalDateTime created = req.getCreatedAt() != null ? req.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(10, Timestamp.valueOf(created));
            if (req.getProcessedAt() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(req.getProcessedAt()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    /**
     * 查询所有待处理且未付款的顾客缺书登记。
     */
    public List<CustomerOutOfStockRequest> findPendingUnpaid() throws SQLException {
        String sql = "SELECT * FROM customer_out_of_stock_request " +
                "WHERE processed_status = 'PENDING' AND is_paid = 0 ORDER BY created_at ASC";
        List<CustomerOutOfStockRequest> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * 按订单查询该订单下所有待处理的顾客缺书登记（无论是否已付款）。
     */
    public List<CustomerOutOfStockRequest> findPendingByOrderId(long orderId) throws SQLException {
        String sql = "SELECT * FROM customer_out_of_stock_request " +
                "WHERE processed_status = 'PENDING' AND order_id = ? ORDER BY created_at ASC";
        List<CustomerOutOfStockRequest> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 更新处理状态及关联的正式缺书记录ID。
     */
    public int updateProcessedStatus(long requestId, String newStatus, Long relatedRecordId) throws SQLException {
        String sql = "UPDATE customer_out_of_stock_request " +
                "SET processed_status = ?, related_record_id = ?, processed_at = ? WHERE request_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            if (relatedRecordId != null) {
                ps.setLong(2, relatedRecordId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, requestId);
            return ps.executeUpdate();
        }
    }

    /**
     * 查询某个客户所有“已处理但尚未通知顾客”的登记。
     */
    public List<CustomerOutOfStockRequest> findUnnotifiedByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT * FROM customer_out_of_stock_request " +
                "WHERE customer_id = ? AND processed_status IN ('ACCEPTED','REJECTED') " +
                "AND customer_notified = 0 ORDER BY created_at ASC";
        List<CustomerOutOfStockRequest> list = new ArrayList<>();
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

    /**
     * 查询某个客户所有已处理（通过/拒绝）的缺书登记记录，用于通知列表展示。
     */
    public List<CustomerOutOfStockRequest> findProcessedByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT * FROM customer_out_of_stock_request " +
                "WHERE customer_id = ? AND processed_status IN ('ACCEPTED','REJECTED') " +
                "ORDER BY created_at DESC";
        List<CustomerOutOfStockRequest> list = new ArrayList<>();
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

    /**
     * 查询等待指定书籍的顾客缺书登记（已付款，已被管理员接受）。
     * 用于采购到货后通知顾客。
     */
    public List<CustomerOutOfStockRequest> findAcceptedPaidByBookId(String bookId) throws SQLException {
        String sql = "SELECT * FROM customer_out_of_stock_request " +
                "WHERE book_id = ? AND processed_status = 'ACCEPTED' AND is_paid = 1 ORDER BY created_at ASC";
        List<CustomerOutOfStockRequest> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 将指定登记标记为“已通知顾客”。
     */
    public int markNotified(List<Long> requestIds) throws SQLException {
        if (requestIds == null || requestIds.isEmpty()) {
            return 0;
        }
        StringBuilder sb = new StringBuilder(
                "UPDATE customer_out_of_stock_request SET customer_notified = 1 WHERE request_id IN (");
        for (int i = 0; i < requestIds.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append('?');
        }
        sb.append(')');
        String sql = sb.toString();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < requestIds.size(); i++) {
                ps.setLong(i + 1, requestIds.get(i));
            }
            return ps.executeUpdate();
        }
    }

    private CustomerOutOfStockRequest mapRow(ResultSet rs) throws SQLException {
        CustomerOutOfStockRequest r = new CustomerOutOfStockRequest();
        r.setRequestId(rs.getLong("request_id"));
        r.setOrderId(rs.getLong("order_id"));
        r.setCustomerId(rs.getLong("customer_id"));
        r.setBookId(rs.getString("book_id"));
        r.setRequestedQty(rs.getInt("requested_qty"));
        r.setCustomerNote(rs.getString("customer_note"));
        r.setPaid(rs.getBoolean("is_paid"));
        r.setProcessedStatus(rs.getString("processed_status"));
        long rid = rs.getLong("related_record_id");
        if (!rs.wasNull()) {
            r.setRelatedRecordId(rid);
        }
        boolean notified = rs.getBoolean("customer_notified");
        r.setCustomerNotified(notified);
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            r.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp processed = rs.getTimestamp("processed_at");
        if (processed != null) {
            r.setProcessedAt(processed.toLocalDateTime());
        }
        return r;
    }
}

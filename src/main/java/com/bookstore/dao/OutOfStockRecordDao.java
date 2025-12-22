package com.bookstore.dao;

import com.bookstore.model.OutOfStockRecord;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 缺书记录数据访问对象
 */
public class OutOfStockRecordDao {

    /**
     * 插入缺书记录，返回生成的 recordId
     */
    public long insert(OutOfStockRecord record) throws SQLException {
        // 规则：同一本书在同一状态（通常为 PENDING）下只保留一条缺书记录，
        // 如果再次插入，则将 required_quantity 追加到现有记录上。
        //
        // 需要配合数据库唯一约束，例如：
        // ALTER TABLE out_of_stock_record
        //   ADD UNIQUE KEY uk_oos_book_status (book_id, status);
        //
        // 通过 ON DUPLICATE KEY UPDATE + LAST_INSERT_ID(record_id) 保证无论是新插入还是追加数量，
        // getGeneratedKeys() 拿到的都是该书对应那条记录的 record_id。
        String sql = "INSERT INTO out_of_stock_record " +
                "(book_id, required_quantity, record_date, source, related_customer_id, status, priority) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "required_quantity = required_quantity + VALUES(required_quantity), " +
                "record_date = VALUES(record_date), " +
                "source = VALUES(source), " +
                "related_customer_id = IFNULL(VALUES(related_customer_id), related_customer_id), " +
                "priority = COALESCE(VALUES(priority), priority), " +
                "record_id = LAST_INSERT_ID(record_id)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getBookId());
            ps.setInt(2, record.getRequiredQuantity());
            ps.setDate(3, Date.valueOf(record.getRecordDate()));
            ps.setString(4, record.getSource());
            if (record.getRelatedCustomerId() != null) {
                ps.setLong(5, record.getRelatedCustomerId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setString(6, record.getStatus());
            if (record.getPriority() != null) {
                ps.setInt(7, record.getPriority());
            } else {
                ps.setNull(7, Types.INTEGER);
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
     * 查询所有待处理的缺书记录
     */
    public List<OutOfStockRecord> findPending() throws SQLException {
        String sql = "SELECT * FROM out_of_stock_record WHERE status = 'PENDING' ORDER BY priority DESC, record_date ASC";
        List<OutOfStockRecord> list = new ArrayList<>();
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
     * 按状态查询缺书记录，供前端灵活使用
     */
    public List<OutOfStockRecord> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM out_of_stock_record WHERE status = ? ORDER BY priority DESC, record_date ASC";
        List<OutOfStockRecord> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 按 recordId 查询
     */
    public OutOfStockRecord findById(long recordId) throws SQLException {
        String sql = "SELECT * FROM out_of_stock_record WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * 更新缺书记录状态
     */
    public int updateStatus(long recordId, String newStatus) throws SQLException {
        String sql = "UPDATE out_of_stock_record SET status = ? WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, recordId);
            return ps.executeUpdate();
        }
    }

    /**
     * 将指定缺书记录安全地标记为 COMPLETED：
     * - 如该书已存在其他 COMPLETED 记录，则删除当前记录以避免唯一约束冲突；
     * - 否则正常将当前记录状态更新为 COMPLETED。
     * 这样既保持 "book_id + status" 唯一约束，又不会在采购到货时抛 Duplicate entry 异常。
     */
    public void completeRecordSafely(long recordId) throws SQLException {
        OutOfStockRecord current = findById(recordId);
        if (current == null) {
            return;
        }
        String bookId = current.getBookId();

        String findSql = "SELECT record_id FROM out_of_stock_record " +
                "WHERE book_id = ? AND status = 'COMPLETED' LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long existingId = rs.getLong("record_id");
                    if (existingId != recordId) {
                        // 已有其它 COMPLETED 记录：
                        // 1. 将所有指向当前记录的采购明细外键重定向到已存在的 COMPLETED 记录
                        try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE purchase_order_item SET related_out_of_stock_id = ? WHERE related_out_of_stock_id = ?")) {
                            up.setLong(1, existingId);
                            up.setLong(2, recordId);
                            up.executeUpdate();
                        }
                        // 2. 删除当前记录，避免违反 (book_id, status) 唯一约束
                        try (PreparedStatement del = conn.prepareStatement(
                                "DELETE FROM out_of_stock_record WHERE record_id = ?")) {
                            del.setLong(1, recordId);
                            del.executeUpdate();
                        }
                        return;
                    }
                }
            }
        }
        // 没有其它 COMPLETED 记录，正常更新状态
        updateStatus(recordId, "COMPLETED");
    }

    private OutOfStockRecord mapRow(ResultSet rs) throws SQLException {
        OutOfStockRecord r = new OutOfStockRecord();
        r.setRecordId(rs.getLong("record_id"));
        r.setBookId(rs.getString("book_id"));
        r.setRequiredQuantity(rs.getInt("required_quantity"));
        Date d = rs.getDate("record_date");
        if (d != null) r.setRecordDate(d.toLocalDate());
        r.setSource(rs.getString("source"));
        long cid = rs.getLong("related_customer_id");
        if (!rs.wasNull()) r.setRelatedCustomerId(cid);
        r.setStatus(rs.getString("status"));
        int p = rs.getInt("priority");
        if (!rs.wasNull()) r.setPriority(p);
        return r;
    }
}


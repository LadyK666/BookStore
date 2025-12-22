package com.bookstore.dao;

import com.bookstore.model.PurchaseOrder;
import com.bookstore.model.PurchaseOrderItem;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购单数据访问对象
 */
public class PurchaseOrderDao {

    /**
     * 创建采购单（含明细），使用事务
     */
    public long createPurchaseOrder(PurchaseOrder order, List<PurchaseOrderItem> items) throws SQLException {
        String sqlOrder = "INSERT INTO purchase_order " +
                "(supplier_id, create_date, expected_date, buyer, estimated_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO purchase_order_item " +
                "(purchase_order_id, book_id, purchase_quantity, purchase_price, related_out_of_stock_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            long orderId;
            // 插入采购单主表
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, order.getSupplierId());
                ps.setDate(2, Date.valueOf(order.getCreateDate()));
                if (order.getExpectedDate() != null) {
                    ps.setDate(3, Date.valueOf(order.getExpectedDate()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setString(4, order.getBuyer());
                ps.setBigDecimal(5, order.getEstimatedAmount());
                ps.setString(6, order.getStatus());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getLong(1);
                    } else {
                        throw new SQLException("Failed to get generated purchase_order_id");
                    }
                }
            }

            // 插入采购明细
            try (PreparedStatement ps = conn.prepareStatement(sqlItem)) {
                for (PurchaseOrderItem item : items) {
                    ps.setLong(1, orderId);
                    ps.setString(2, item.getBookId());
                    ps.setInt(3, item.getPurchaseQuantity());
                    ps.setBigDecimal(4, item.getPurchasePrice());
                    if (item.getRelatedOutOfStockId() != null) {
                        ps.setLong(5, item.getRelatedOutOfStockId());
                    } else {
                        ps.setNull(5, Types.BIGINT);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * 查询所有采购单
     */
    public List<PurchaseOrder> findAll() throws SQLException {
        String sql = "SELECT * FROM purchase_order ORDER BY purchase_order_id DESC";
        List<PurchaseOrder> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        }
        return list;
    }

    /**
     * 按主键查采购单
     */
    public PurchaseOrder findById(long purchaseOrderId) throws SQLException {
        String sql = "SELECT * FROM purchase_order WHERE purchase_order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        }
        return null;
    }

    /**
     * 按采购单号查明细
     */
    public List<PurchaseOrderItem> findItemsByOrderId(long purchaseOrderId) throws SQLException {
        String sql = "SELECT * FROM purchase_order_item WHERE purchase_order_id = ?";
        List<PurchaseOrderItem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, purchaseOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapItem(rs));
                }
            }
        }
        return list;
    }

    /**
     * 更新采购单状态
     */
    public int updateStatus(long purchaseOrderId, String newStatus) throws SQLException {
        String sql = "UPDATE purchase_order SET status = ? WHERE purchase_order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, purchaseOrderId);
            return ps.executeUpdate();
        }
    }

    private PurchaseOrder mapOrder(ResultSet rs) throws SQLException {
        PurchaseOrder o = new PurchaseOrder();
        o.setPurchaseOrderId(rs.getLong("purchase_order_id"));
        o.setSupplierId(rs.getLong("supplier_id"));
        Date cd = rs.getDate("create_date");
        if (cd != null) o.setCreateDate(cd.toLocalDate());
        Date ed = rs.getDate("expected_date");
        if (ed != null) o.setExpectedDate(ed.toLocalDate());
        o.setBuyer(rs.getString("buyer"));
        o.setEstimatedAmount(rs.getBigDecimal("estimated_amount"));
        o.setStatus(rs.getString("status"));
        return o;
    }

    private PurchaseOrderItem mapItem(ResultSet rs) throws SQLException {
        PurchaseOrderItem i = new PurchaseOrderItem();
        i.setPurchaseOrderId(rs.getLong("purchase_order_id"));
        i.setBookId(rs.getString("book_id"));
        i.setPurchaseQuantity(rs.getInt("purchase_quantity"));
        i.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        long oos = rs.getLong("related_out_of_stock_id");
        if (!rs.wasNull()) i.setRelatedOutOfStockId(oos);
        return i;
    }
}


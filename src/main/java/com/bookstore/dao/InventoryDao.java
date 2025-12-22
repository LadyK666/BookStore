package com.bookstore.dao;

import com.bookstore.model.Inventory;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存数据访问对象
 */
public class InventoryDao {

    /**
     * 增加库存（采购到货）
     */
    public int increaseQuantity(String bookId, int delta) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, bookId);
            return ps.executeUpdate();
        }
    }

    /**
     * 减少库存（发货）
     */
    public int decreaseQuantity(String bookId, int delta) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE book_id = ? AND quantity >= ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, bookId);
            ps.setInt(3, delta);
            return ps.executeUpdate();
        }
    }

    /**
     * 查询当前库存数量
     */
    public int getQuantity(String bookId) throws SQLException {
        String sql = "SELECT quantity FROM inventory WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }
        return 0;
    }

    /**
     * 查询安全库存量
     */
    public int getSafetyStock(String bookId) throws SQLException {
        String sql = "SELECT safety_stock FROM inventory WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("safety_stock");
                }
            }
        }
        return 0;
    }

    /**
     * 更新安全库存量
     */
    public int updateSafetyStock(String bookId, int safetyStock) throws SQLException {
        String sql = "UPDATE inventory SET safety_stock = ? WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, safetyStock);
            ps.setString(2, bookId);
            return ps.executeUpdate();
        }
    }

    /**
     * 查询所有库存记录
     */
    public List<Inventory> findAll() throws SQLException {
        String sql = "SELECT book_id, quantity, safety_stock, location_code FROM inventory ORDER BY book_id";
        List<Inventory> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setBookId(rs.getString("book_id"));
                inv.setQuantity(rs.getInt("quantity"));
                inv.setSafetyStock(rs.getInt("safety_stock"));
                inv.setLocationCode(rs.getString("location_code"));
                list.add(inv);
            }
        }
        return list;
    }

    /**
     * 新增库存记录
     */
    public int insert(Inventory inv) throws SQLException {
        String sql = "INSERT INTO inventory (book_id, quantity, safety_stock, location_code) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inv.getBookId());
            ps.setInt(2, inv.getQuantity());
            ps.setInt(3, inv.getSafetyStock());
            ps.setString(4, inv.getLocationCode());
            return ps.executeUpdate();
        }
    }
}


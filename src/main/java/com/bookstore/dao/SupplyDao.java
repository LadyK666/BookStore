package com.bookstore.dao;

import com.bookstore.model.Supply;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Supply 数据访问类。
 * 提供按供应商或按书号查询供货信息的方法。
 */
public class SupplyDao {

    public List<Supply> findBySupplierId(long supplierId) throws SQLException {
        String sql = "SELECT supplier_id, book_id, supply_price, lead_time_days, is_primary " +
                "FROM supply WHERE supplier_id = ?";
        List<Supply> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Supply> findByBookId(String bookId) throws SQLException {
        String sql = "SELECT supplier_id, book_id, supply_price, lead_time_days, is_primary " +
                "FROM supply WHERE book_id = ?";
        List<Supply> list = new ArrayList<>();
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
     * 为某本书新增一个供货关系。
     * 如标记为主供货商，则会先将该书的其他供货关系 is_primary 置为 0。
     */
    public void insert(Supply supply) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                if (supply.isPrimary()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE supply SET is_primary = 0 WHERE book_id = ?")) {
                        ps.setString(1, supply.getBookId());
                        ps.executeUpdate();
                    }
                }

                String sql = "INSERT INTO supply (supplier_id, book_id, supply_price, lead_time_days, is_primary) " +
                        "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, supply.getSupplierId());
                    ps.setString(2, supply.getBookId());
                    ps.setBigDecimal(3, supply.getSupplyPrice());
                    if (supply.getLeadTimeDays() != null) {
                        ps.setInt(4, supply.getLeadTimeDays());
                    } else {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    }
                    ps.setBoolean(5, supply.isPrimary());
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void delete(long supplierId, String bookId) throws SQLException {
        String sql = "DELETE FROM supply WHERE supplier_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, supplierId);
            ps.setString(2, bookId);
            ps.executeUpdate();
        }
    }

    /**
     * 删除指定书籍的所有供货关系
     */
    public void deleteByBookId(String bookId) throws SQLException {
        String sql = "DELETE FROM supply WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.executeUpdate();
        }
    }

    /**
     * 更新某条供货关系（按 supplier_id + book_id）。
     */
    public void update(Supply supply) throws SQLException {
        String sql = "UPDATE supply SET supply_price = ?, lead_time_days = ?, is_primary = ? " +
                "WHERE supplier_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, supply.getSupplyPrice());
            if (supply.getLeadTimeDays() != null) {
                ps.setInt(2, supply.getLeadTimeDays());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setBoolean(3, supply.isPrimary());
            ps.setLong(4, supply.getSupplierId());
            ps.setString(5, supply.getBookId());
            ps.executeUpdate();
        }
    }

    private Supply mapRow(ResultSet rs) throws SQLException {
        Supply s = new Supply();
        s.setSupplierId(rs.getLong("supplier_id"));
        s.setBookId(rs.getString("book_id"));
        s.setSupplyPrice(rs.getBigDecimal("supply_price"));
        int lead = rs.getInt("lead_time_days");
        s.setLeadTimeDays(rs.wasNull() ? null : lead);
        s.setPrimary(rs.getBoolean("is_primary"));
        return s;
    }
}



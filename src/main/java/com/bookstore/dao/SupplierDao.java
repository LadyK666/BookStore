package com.bookstore.dao;

import com.bookstore.model.Supplier;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Supplier 数据访问类。
 * 本阶段提供：查询全部、按主键查询。
 */
public class SupplierDao {

    public List<Supplier> findAll() throws SQLException {
        String sql = "SELECT supplier_id, supplier_name, contact_person, phone, email, address, payment_terms, cooperation_status " +
                "FROM supplier ORDER BY supplier_id";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Supplier findById(long supplierId) throws SQLException {
        String sql = "SELECT supplier_id, supplier_name, contact_person, phone, email, address, payment_terms, cooperation_status " +
                "FROM supplier WHERE supplier_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * 新增供应商
     */
    public int insert(Supplier s) throws SQLException {
        String sql = "INSERT INTO supplier (supplier_name, contact_person, phone, email, address, payment_terms, cooperation_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setString(6, s.getPaymentTerms());
            ps.setString(7, s.getCooperationStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        s.setSupplierId(keys.getLong(1));
                    }
                }
            }
            return rows;
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getLong("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setPaymentTerms(rs.getString("payment_terms"));
        s.setCooperationStatus(rs.getString("cooperation_status"));
        return s;
    }
}



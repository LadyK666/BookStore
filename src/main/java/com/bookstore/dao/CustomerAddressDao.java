package com.bookstore.dao;

import com.bookstore.model.CustomerAddress;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户地址 DAO，负责对 customer_address 表进行增删改查。
 */
public class CustomerAddressDao {

    public List<CustomerAddress> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT address_id, customer_id, receiver, phone, province, city, district, detail, is_default " +
                "FROM customer_address WHERE customer_id = ? ORDER BY is_default DESC, address_id DESC";
        List<CustomerAddress> list = new ArrayList<>();
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

    public void insert(CustomerAddress addr) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // 如果插入的是默认地址，先把该客户的其他地址取消默认
                if (addr.isDefault()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE customer_address SET is_default = 0 WHERE customer_id = ?")) {
                        ps.setLong(1, addr.getCustomerId());
                        ps.executeUpdate();
                    }
                }

                String sql = "INSERT INTO customer_address " +
                        "(customer_id, receiver, phone, province, city, district, detail, is_default) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, addr.getCustomerId());
                    ps.setString(2, addr.getReceiver());
                    ps.setString(3, addr.getPhone());
                    ps.setString(4, addr.getProvince());
                    ps.setString(5, addr.getCity());
                    ps.setString(6, addr.getDistrict());
                    ps.setString(7, addr.getDetail());
                    ps.setBoolean(8, addr.isDefault());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            addr.setAddressId(keys.getLong(1));
                        }
                    }
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

    public void updateDefault(long customerId, long addressId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE customer_address SET is_default = 0 WHERE customer_id = ?")) {
                    ps.setLong(1, customerId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE customer_address SET is_default = 1 WHERE address_id = ? AND customer_id = ?")) {
                    ps.setLong(1, addressId);
                    ps.setLong(2, customerId);
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

    public void delete(long addressId) throws SQLException {
        String sql = "DELETE FROM customer_address WHERE address_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, addressId);
            ps.executeUpdate();
        }
    }

    private CustomerAddress mapRow(ResultSet rs) throws SQLException {
        CustomerAddress a = new CustomerAddress();
        a.setAddressId(rs.getLong("address_id"));
        a.setCustomerId(rs.getLong("customer_id"));
        a.setReceiver(rs.getString("receiver"));
        a.setPhone(rs.getString("phone"));
        a.setProvince(rs.getString("province"));
        a.setCity(rs.getString("city"));
        a.setDistrict(rs.getString("district"));
        a.setDetail(rs.getString("detail"));
        a.setDefault(rs.getBoolean("is_default"));
        return a;
    }
}



package com.bookstore.dao;

import com.bookstore.model.Customer;
import com.bookstore.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer 数据访问类（DAO），负责对 customer 表进行基础操作。
 * 本阶段实现：
 *  - 查询全部客户
 *  - 按用户名查询（用于登录/校验重名）
 *  - 新增客户（简单“注册”）
 */
public class CustomerDao {

    /**
     * 查询所有客户（按 customer_id 排序）。
     */
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT customer_id, username, password_hash, real_name, mobile_phone, email, " +
                "account_balance, total_consumption, registration_time, account_status, credit_level_id " +
                "FROM customer ORDER BY customer_id";
        List<Customer> list = new ArrayList<>();
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
     * 根据用户名查询客户（用户名在表中是唯一的）。
     */
    public Customer findByUsername(String username) throws SQLException {
        String sql = "SELECT customer_id, username, password_hash, real_name, mobile_phone, email, " +
                "account_balance, total_consumption, registration_time, account_status, credit_level_id " +
                "FROM customer WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * 新增客户（简单注册，不含密码加密逻辑，课程阶段可先用明文/固定字符串）。
     */
    public int insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customer " +
                "(username, password_hash, real_name, mobile_phone, email, account_balance, total_consumption, credit_level_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getUsername());
            ps.setString(2, c.getPasswordHash());
            ps.setString(3, c.getRealName());
            ps.setString(4, c.getMobilePhone());
            ps.setString(5, c.getEmail());
            ps.setBigDecimal(6, c.getAccountBalance() != null ? c.getAccountBalance() : BigDecimal.ZERO);
            ps.setBigDecimal(7, c.getTotalConsumption() != null ? c.getTotalConsumption() : BigDecimal.ZERO);
            ps.setInt(8, c.getCreditLevelId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        c.setCustomerId(keys.getLong(1));
                    }
                }
            }
            return rows;
        }
    }

    /**
     * 根据主键查询客户。
     */
    public Customer findById(long customerId) throws SQLException {
        String sql = "SELECT customer_id, username, password_hash, real_name, mobile_phone, email, " +
                "account_balance, total_consumption, registration_time, account_status, credit_level_id " +
                "FROM customer WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * 更新客户账户余额（不改变其他字段）。
     */
    public int updateAccountBalance(long customerId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE customer SET account_balance = ? WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, customerId);
            return ps.executeUpdate();
        }
    }

    /**
     * 更新客户信用等级。
     */
    public int updateCreditLevel(long customerId, int creditLevelId) throws SQLException {
        String sql = "UPDATE customer SET credit_level_id = ? WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creditLevelId);
            ps.setLong(2, customerId);
            return ps.executeUpdate();
        }
    }

    /**
     * 增加客户累积消费金额（在原有基础上累加）。
     */
    public int addTotalConsumption(long customerId, BigDecimal amount) throws SQLException {
        String sql = "UPDATE customer SET total_consumption = total_consumption + ? WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, customerId);
            return ps.executeUpdate();
        }
    }

    /**
     * 更新客户基本信息（真实姓名、手机、邮箱）。
     */
    public int updateCustomerInfo(long customerId, String realName, String mobilePhone, String email) throws SQLException {
        String sql = "UPDATE customer SET real_name = ?, mobile_phone = ?, email = ? WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, realName);
            ps.setString(2, mobilePhone);
            ps.setString(3, email);
            ps.setLong(4, customerId);
            return ps.executeUpdate();
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setUsername(rs.getString("username"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setRealName(rs.getString("real_name"));
        c.setMobilePhone(rs.getString("mobile_phone"));
        c.setEmail(rs.getString("email"));
        c.setAccountBalance(rs.getBigDecimal("account_balance"));
        c.setTotalConsumption(rs.getBigDecimal("total_consumption"));
        Timestamp ts = rs.getTimestamp("registration_time");
        if (ts != null) {
            c.setRegistrationTime(ts.toLocalDateTime());
        }
        c.setAccountStatus(rs.getString("account_status"));
        c.setCreditLevelId(rs.getInt("credit_level_id"));
        return c;
    }
}



package com.bookstore.dao;

import com.bookstore.model.CreditLevel;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CreditLevel 数据访问类，当前阶段仅支持按主键查询。
 */
public class CreditLevelDao {

    public CreditLevel findById(int levelId) throws SQLException {
        String sql = "SELECT level_id, level_name, discount_rate, allow_overdraft, overdraft_limit, upgrade_condition " +
                "FROM credit_level WHERE level_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CreditLevel level = new CreditLevel();
                    level.setLevelId(rs.getInt("level_id"));
                    level.setLevelName(rs.getString("level_name"));
                    level.setDiscountRate(rs.getBigDecimal("discount_rate"));
                    level.setAllowOverdraft(rs.getBoolean("allow_overdraft"));
                    level.setOverdraftLimit(rs.getBigDecimal("overdraft_limit"));
                    level.setUpgradeCondition(rs.getString("upgrade_condition"));
                    return level;
                }
                return null;
            }
        }
    }
}



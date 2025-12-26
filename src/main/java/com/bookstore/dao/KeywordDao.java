package com.bookstore.dao;

import com.bookstore.model.Keyword;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 关键字相关 DAO。
 */
public class KeywordDao {

    public Long insert(Keyword keyword) throws SQLException {
        String sql = "INSERT INTO keyword (keyword_text) VALUES (?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, keyword.getKeywordText());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    keyword.setKeywordId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<Keyword> findByBookId(String bookId) throws SQLException {
        String sql = "SELECT k.keyword_id, k.keyword_text " +
                "FROM keyword k JOIN book_keyword bk ON k.keyword_id = bk.keyword_id " +
                "WHERE bk.book_id = ? ORDER BY k.keyword_text";
        List<Keyword> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Keyword k = new Keyword();
                    k.setKeywordId(rs.getLong("keyword_id"));
                    k.setKeywordText(rs.getString("keyword_text"));
                    list.add(k);
                }
            }
        }
        return list;
    }

    /**
     * 更新关键字文本。
     */
    public int update(Keyword keyword) throws SQLException {
        String sql = "UPDATE keyword SET keyword_text = ? WHERE keyword_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword.getKeywordText());
            ps.setLong(2, keyword.getKeywordId());
            return ps.executeUpdate();
        }
    }

    /**
     * 按关键字文本模糊查询其关联的书目编号集合。
     */
    public Set<String> findBookIdsByKeywordTextLike(String keywordText) throws SQLException {
        String sql = "SELECT DISTINCT bk.book_id " +
                "FROM keyword k JOIN book_keyword bk ON k.keyword_id = bk.keyword_id " +
                "WHERE k.keyword_text LIKE ?";
        Set<String> result = new HashSet<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keywordText.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("book_id"));
                }
            }
        }
        return result;
    }

    /**
     * 按多个关键字文本查询书目，并返回匹配数达到最低要求的书目。
     * 
     * @param keywords 逗号分隔的关键字列表
     * @param minMatch 最低匹配数（如：传入3个关键字，minMatch=2表示至少匹配2个）
     * @return 书目ID到匹配数的映射
     */
    public Map<String, Integer> findBookIdsByKeywordsWithMinMatch(List<String> keywords, int minMatch)
            throws SQLException {
        if (keywords == null || keywords.isEmpty()) {
            return new HashMap<>();
        }

        // 构建 SQL，统计每本书匹配了多少个关键字
        StringBuilder sql = new StringBuilder(
                "SELECT bk.book_id, COUNT(DISTINCT k.keyword_id) as match_count " +
                        "FROM keyword k JOIN book_keyword bk ON k.keyword_id = bk.keyword_id " +
                        "WHERE ");
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < keywords.size(); i++) {
            conditions.add("k.keyword_text LIKE ?");
        }
        sql.append("(").append(String.join(" OR ", conditions)).append(")");
        sql.append(" GROUP BY bk.book_id HAVING match_count >= ?");
        sql.append(" ORDER BY match_count DESC");

        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            for (String kw : keywords) {
                ps.setString(paramIdx++, "%" + kw.trim() + "%");
            }
            ps.setInt(paramIdx, minMatch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("book_id"), rs.getInt("match_count"));
                }
            }
        }
        return result;
    }
}

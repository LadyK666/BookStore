package com.bookstore.dao;

import com.bookstore.model.Keyword;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
}



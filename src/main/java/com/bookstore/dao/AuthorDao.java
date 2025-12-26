package com.bookstore.dao;

import com.bookstore.model.Author;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 作者相关 DAO。
 */
public class AuthorDao {

    public Long insert(Author author) throws SQLException {
        String sql = "INSERT INTO author (author_name, nationality, biography) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, author.getAuthorName());
            ps.setString(2, author.getNationality());
            ps.setString(3, author.getBiography());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    author.setAuthorId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<Author> findByBookId(String bookId) throws SQLException {
        String sql = "SELECT a.author_id, a.author_name, a.nationality, a.biography, ba.author_order " +
                "FROM author a JOIN book_author ba ON a.author_id = ba.author_id " +
                "WHERE ba.book_id = ? ORDER BY ba.author_order";
        List<Author> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Author a = new Author();
                    a.setAuthorId(rs.getLong("author_id"));
                    a.setAuthorName(rs.getString("author_name"));
                    a.setNationality(rs.getString("nationality"));
                    a.setBiography(rs.getString("biography"));
                    a.setAuthorOrder(rs.getInt("author_order"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    /**
     * 更新作者基础信息。
     */
    public int update(Author author) throws SQLException {
        String sql = "UPDATE author SET author_name = ?, nationality = ?, biography = ? WHERE author_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, author.getAuthorName());
            ps.setString(2, author.getNationality());
            ps.setString(3, author.getBiography());
            ps.setLong(4, author.getAuthorId());
            return ps.executeUpdate();
        }
    }

    /**
     * 按作者姓名模糊查询其参与的书目编号集合。
     */
    public Set<String> findBookIdsByAuthorNameLike(String nameKeyword) throws SQLException {
        String sql = "SELECT DISTINCT ba.book_id " +
                "FROM author a JOIN book_author ba ON a.author_id = ba.author_id " +
                "WHERE a.author_name LIKE ?";
        Set<String> result = new HashSet<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nameKeyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("book_id"));
                }
            }
        }
        return result;
    }

    /**
     * 按作者姓名和作者顺序查询其参与的书目编号集合。
     * 
     * @param nameKeyword 作者姓名关键字
     * @param authorOrder 作者顺序（1=第一作者, 2=第二作者, 等）；传0或null表示不限
     */
    public Set<String> findBookIdsByAuthorNameLikeWithOrder(String nameKeyword, Integer authorOrder)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT ba.book_id " +
                        "FROM author a JOIN book_author ba ON a.author_id = ba.author_id " +
                        "WHERE a.author_name LIKE ?");
        if (authorOrder != null && authorOrder > 0) {
            sql.append(" AND ba.author_order = ?");
        }
        Set<String> result = new HashSet<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + nameKeyword.trim() + "%");
            if (authorOrder != null && authorOrder > 0) {
                ps.setInt(2, authorOrder);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("book_id"));
                }
            }
        }
        return result;
    }
}

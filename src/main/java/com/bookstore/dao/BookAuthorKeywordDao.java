package com.bookstore.dao;

import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 负责维护 book_author 与 book_keyword 关系的简单 DAO。
 */
public class BookAuthorKeywordDao {

    /**
     * 为某本书关联一位作者及作者顺序。
     */
    public int addBookAuthor(String bookId, long authorId, int authorOrder) throws SQLException {
        String sql = "INSERT INTO book_author (book_id, author_id, author_order) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.setLong(2, authorId);
            ps.setInt(3, authorOrder);
            return ps.executeUpdate();
        }
    }

    /**
     * 为某本书关联一个关键字。
     */
    public int addBookKeyword(String bookId, long keywordId) throws SQLException {
        String sql = "INSERT INTO book_keyword (book_id, keyword_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.setLong(2, keywordId);
            return ps.executeUpdate();
        }
    }

    /**
     * 解除某本书与指定作者的关联。
     */
    public int removeBookAuthor(String bookId, long authorId) throws SQLException {
        String sql = "DELETE FROM book_author WHERE book_id = ? AND author_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.setLong(2, authorId);
            return ps.executeUpdate();
        }
    }

    /**
     * 更新某本书中某位作者的作者序号。
     */
    public int updateBookAuthorOrder(String bookId, long authorId, int newOrder) throws SQLException {
        String sql = "UPDATE book_author SET author_order = ? WHERE book_id = ? AND author_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newOrder);
            ps.setString(2, bookId);
            ps.setLong(3, authorId);
            return ps.executeUpdate();
        }
    }

    /**
     * 解除某本书与指定关键字的关联。
     */
    public int removeBookKeyword(String bookId, long keywordId) throws SQLException {
        String sql = "DELETE FROM book_keyword WHERE book_id = ? AND keyword_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.setLong(2, keywordId);
            return ps.executeUpdate();
        }
    }
}



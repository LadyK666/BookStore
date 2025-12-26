package com.bookstore.dao;

import com.bookstore.model.Book;
import com.bookstore.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Book 数据访问类（DAO），负责对 book 表进行增删改查操作。
 * 本阶段作为代码骨架，采用最基础的 JDBC 写法，后续可以根据需要再重构。
 */
public class BookDao {

    /**
     * 查询所有图书（按 book_id 排序）。
     */
    public List<Book> findAll() throws SQLException {
        String sql = "SELECT book_id, isbn, title, publisher, publish_date, edition, price, status, cover_image_url, catalog, series_flag, parent_book_id FROM book ORDER BY COALESCE(parent_book_id, book_id), series_flag DESC, book_id";
        List<Book> list = new ArrayList<>();

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
     * 获取图书总数。
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM book";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * 按多条件查询图书。
     * 所有参数均可为空/空串，表示不按该条件过滤；
     * - bookId：按书号前缀/包含模糊匹配
     * - title：按书名模糊匹配（LIKE）
     * - publisher：按出版社模糊匹配（LIKE）
     */
    public List<Book> findByConditions(String bookId, String title, String publisher) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT book_id, isbn, title, publisher, publish_date, edition, price, status, cover_image_url, catalog, series_flag, parent_book_id FROM book WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (bookId != null && !bookId.trim().isEmpty()) {
            sql.append(" AND book_id LIKE ?");
            params.add("%" + bookId.trim() + "%");
        }
        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + title.trim() + "%");
        }
        if (publisher != null && !publisher.trim().isEmpty()) {
            sql.append(" AND publisher LIKE ?");
            params.add("%" + publisher.trim() + "%");
        }

        sql.append(" ORDER BY COALESCE(parent_book_id, book_id), series_flag DESC, book_id");

        List<Book> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        }
    }

    /**
     * 根据主键查询图书。
     */
    public Book findById(String bookId) throws SQLException {
        String sql = "SELECT book_id, isbn, title, publisher, publish_date, edition, price, status, cover_image_url, catalog, series_flag, parent_book_id FROM book WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * 新增一本图书。
     */
    public int insert(Book book) throws SQLException {
        String sql = "INSERT INTO book (book_id, isbn, title, publisher, publish_date, edition, price, status, cover_image_url, catalog, series_flag, parent_book_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getBookId());
            ps.setString(2, book.getIsbn());
            ps.setString(3, book.getTitle());
            ps.setString(4, book.getPublisher());
            if (book.getPublishDate() != null) {
                ps.setDate(5, Date.valueOf(book.getPublishDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, book.getEdition());
            ps.setBigDecimal(7, book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO);
            ps.setString(8, book.getStatus());
            ps.setString(9, book.getCoverImageUrl());
            ps.setString(10, book.getCatalog());
            ps.setBoolean(11, book.isSeriesFlag());
            ps.setString(12, book.getParentBookId());

            return ps.executeUpdate();
        }
    }

    /**
     * 更新图书信息（根据 book_id）。
     */
    public int update(Book book) throws SQLException {
        String sql = "UPDATE book SET isbn = ?, title = ?, publisher = ?, publish_date = ?, edition = ?, price = ?, status = ?, cover_image_url = ?, catalog = ?, series_flag = ?, parent_book_id = ? "
                + "WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getPublisher());
            if (book.getPublishDate() != null) {
                ps.setDate(4, Date.valueOf(book.getPublishDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, book.getEdition());
            ps.setBigDecimal(6, book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO);
            ps.setString(7, book.getStatus());
            ps.setString(8, book.getCoverImageUrl());
            ps.setString(9, book.getCatalog());
            ps.setBoolean(10, book.isSeriesFlag());
            ps.setString(11, book.getParentBookId());
            ps.setString(12, book.getBookId());

            return ps.executeUpdate();
        }
    }

    /**
     * 根据主键删除一本图书。
     */
    public int deleteById(String bookId) throws SQLException {
        String sql = "DELETE FROM book WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            return ps.executeUpdate();
        }
    }

    /**
     * 将 ResultSet 当前行映射为 Book 对象。
     */
    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getString("book_id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setPublisher(rs.getString("publisher"));
        Date publishDate = rs.getDate("publish_date");
        if (publishDate != null) {
            b.setPublishDate(publishDate.toLocalDate());
        }
        b.setEdition(rs.getString("edition"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setStatus(rs.getString("status"));
        b.setCoverImageUrl(rs.getString("cover_image_url"));
        b.setCatalog(rs.getString("catalog"));
        // 丛书字段
        try {
            b.setSeriesFlag(rs.getBoolean("series_flag"));
            b.setParentBookId(rs.getString("parent_book_id"));
        } catch (SQLException ignored) {
            // 某些查询可能不包含这些字段
        }
        return b;
    }

    /**
     * 查询丛书的子书目列表。
     */
    public List<Book> findChildBooks(String parentBookId) throws SQLException {
        String sql = "SELECT book_id, isbn, title, publisher, publish_date, edition, price, status, cover_image_url, catalog, series_flag, parent_book_id FROM book WHERE parent_book_id = ? ORDER BY book_id";
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, parentBookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 查询所有丛书（series_flag = 1）。
     */
    public List<Book> findSeriesBooks() throws SQLException {
        String sql = "SELECT book_id, isbn, title, publisher, publish_date, edition, price, status, cover_image_url, catalog, series_flag, parent_book_id FROM book WHERE series_flag = 1 ORDER BY book_id";
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
}

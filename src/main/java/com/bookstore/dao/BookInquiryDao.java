package com.bookstore.dao;

import com.bookstore.model.BookInquiryRequest;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookInquiryDao {

    public void createInquiry(BookInquiryRequest inquiry) throws SQLException {
        String sql = "INSERT INTO book_inquiry_request (customer_id, book_title, book_author, publisher, isbn, quantity, customer_note) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, inquiry.getCustomerId());
            stmt.setString(2, inquiry.getBookTitle());
            stmt.setString(3, inquiry.getBookAuthor());
            stmt.setString(4, inquiry.getPublisher());
            stmt.setString(5, inquiry.getIsbn());
            stmt.setInt(6, inquiry.getQuantity());
            stmt.setString(7, inquiry.getCustomerNote());
            stmt.executeUpdate();
        }
    }

    public List<BookInquiryRequest> getCustomerInquiries(Long customerId) throws SQLException {
        List<BookInquiryRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM book_inquiry_request WHERE customer_id = ? ORDER BY inquiry_time DESC";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInquiry(rs));
                }
            }
        }
        return list;
    }

    public List<BookInquiryRequest> getAllInquiries() throws SQLException {
        List<BookInquiryRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM book_inquiry_request ORDER BY inquiry_time DESC";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToInquiry(rs));
            }
        }
        return list;
    }

    public BookInquiryRequest getInquiryById(Long id) throws SQLException {
        String sql = "SELECT * FROM book_inquiry_request WHERE inquiry_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInquiry(rs);
                }
            }
        }
        return null;
    }

    public void updateInquiry(BookInquiryRequest inquiry) throws SQLException {
        String sql = "UPDATE book_inquiry_request SET status = ?, admin_reply = ?, quoted_price = ?, reply_time = ? WHERE inquiry_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, inquiry.getStatus());
            stmt.setString(2, inquiry.getAdminReply());
            stmt.setBigDecimal(3, inquiry.getQuotedPrice());
            stmt.setTimestamp(4, inquiry.getReplyTime());
            stmt.setLong(5, inquiry.getInquiryId());
            stmt.executeUpdate();
        }
    }

    private BookInquiryRequest mapResultSetToInquiry(ResultSet rs) throws SQLException {
        BookInquiryRequest inquiry = new BookInquiryRequest();
        inquiry.setInquiryId(rs.getLong("inquiry_id"));
        inquiry.setCustomerId(rs.getLong("customer_id"));
        inquiry.setBookTitle(rs.getString("book_title"));
        inquiry.setBookAuthor(rs.getString("book_author"));
        inquiry.setPublisher(rs.getString("publisher"));
        inquiry.setIsbn(rs.getString("isbn"));
        inquiry.setQuantity(rs.getInt("quantity"));
        inquiry.setCustomerNote(rs.getString("customer_note"));
        inquiry.setInquiryTime(rs.getTimestamp("inquiry_time"));
        inquiry.setStatus(rs.getString("status"));
        inquiry.setAdminReply(rs.getString("admin_reply"));
        inquiry.setQuotedPrice(rs.getBigDecimal("quoted_price"));
        inquiry.setReplyTime(rs.getTimestamp("reply_time"));
        return inquiry;
    }
}

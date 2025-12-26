package com.bookstore.dao;

import com.bookstore.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车 DAO，负责对 shopping_cart 表进行增删改查。
 */
public class ShoppingCartDao {

    public static class CartItem {
        private long cartItemId;
        private long customerId;
        private String bookId;
        private int quantity;
        private String bookTitle;
        private BigDecimal unitPrice;

        public long getCartItemId() {
            return cartItemId;
        }

        public void setCartItemId(long cartItemId) {
            this.cartItemId = cartItemId;
        }

        public long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(long customerId) {
            this.customerId = customerId;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    /**
     * 获取用户购物车列表（带图书信息）
     */
    public List<CartItem> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT c.cart_item_id, c.customer_id, c.book_id, c.quantity, b.title, b.price " +
                "FROM shopping_cart c JOIN book b ON c.book_id = b.book_id " +
                "WHERE c.customer_id = ? ORDER BY c.created_at DESC";
        List<CartItem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getLong("cart_item_id"));
                    item.setCustomerId(rs.getLong("customer_id"));
                    item.setBookId(rs.getString("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setBookTitle(rs.getString("title"));
                    item.setUnitPrice(rs.getBigDecimal("price"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * 添加或更新购物车商品（如果已存在则累加数量）
     */
    public void upsert(long customerId, String bookId, int quantity) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // 先尝试更新已存在的记录
            String updateSql = "UPDATE shopping_cart SET quantity = quantity + ? WHERE customer_id = ? AND book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, quantity);
                ps.setLong(2, customerId);
                ps.setString(3, bookId);
                int updated = ps.executeUpdate();

                // 如果没有更新任何记录，说明不存在，需要插入
                if (updated == 0) {
                    String insertSql = "INSERT INTO shopping_cart (customer_id, book_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setLong(1, customerId);
                        insertPs.setString(2, bookId);
                        insertPs.setInt(3, quantity);
                        insertPs.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * 更新购物车商品数量（设置为指定值）
     */
    public void updateQuantity(long customerId, String bookId, int quantity) throws SQLException {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE customer_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, customerId);
            ps.setString(3, bookId);
            ps.executeUpdate();
        }
    }

    /**
     * 删除购物车中的某个商品
     */
    public void delete(long customerId, String bookId) throws SQLException {
        String sql = "DELETE FROM shopping_cart WHERE customer_id = ? AND book_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setString(2, bookId);
            ps.executeUpdate();
        }
    }

    /**
     * 清空用户购物车
     */
    public void clearCart(long customerId) throws SQLException {
        String sql = "DELETE FROM shopping_cart WHERE customer_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.executeUpdate();
        }
    }
}

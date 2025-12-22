package com.bookstore.dao;

import com.bookstore.model.SalesOrder;
import com.bookstore.model.SalesOrderItem;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SalesOrder / SalesOrderItem 数据访问类。
 * 本阶段实现：在一个事务中创建订单主表和明细表记录，并支持简单查询回显。
 */
public class SalesOrderDao {

    /**
     * 在一个事务中创建订单及其明细。
     * 调用前应保证 order 中的金额字段、优惠快照等已经计算好。
     */
    public void createOrder(SalesOrder order, List<SalesOrderItem> items) throws SQLException {
        String insertOrderSql = "INSERT INTO sales_order " +
                "(customer_id, order_time, order_status, goods_amount, discount_rate_snapshot, payable_amount, " +
                "shipping_address_snapshot, customer_note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String insertItemSql = "INSERT INTO sales_order_item " +
                "(order_id, book_id, quantity, shipped_quantity, received_quantity, unit_price, sub_amount, item_status) " +
                "VALUES (?, ?, ?, 0, 0, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // 插入订单主表
                try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    psOrder.setLong(1, order.getCustomerId());
                    psOrder.setTimestamp(2, Timestamp.valueOf(
                            order.getOrderTime() != null ? order.getOrderTime() : LocalDateTime.now()));
                    psOrder.setString(3, order.getOrderStatus());
                    psOrder.setBigDecimal(4, order.getGoodsAmount());
                    psOrder.setBigDecimal(5, order.getDiscountRateSnapshot());
                    psOrder.setBigDecimal(6, order.getPayableAmount());
                    psOrder.setString(7, order.getShippingAddressSnapshot());
                    psOrder.setString(8, order.getCustomerNote());

                    psOrder.executeUpdate();
                    try (ResultSet keys = psOrder.getGeneratedKeys()) {
                        if (keys.next()) {
                            long orderId = keys.getLong(1);
                            order.setOrderId(orderId);
                            // 插入明细
                            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                                for (SalesOrderItem item : items) {
                                    item.setOrderId(orderId);
                                    psItem.setLong(1, item.getOrderId());
                                    psItem.setString(2, item.getBookId());
                                    psItem.setInt(3, item.getQuantity());
                                    psItem.setBigDecimal(4, item.getUnitPrice());
                                    psItem.setBigDecimal(5, item.getSubAmount());
                                    psItem.setString(6, item.getItemStatus());
                                    psItem.addBatch();
                                }
                                psItem.executeBatch();
                            }
                        } else {
                            throw new SQLException("创建订单失败，未获取到生成的主键。");
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

    /**
     * 简单查询：根据 order_id 查询订单主表信息。
     */
    public SalesOrder findOrderById(long orderId) throws SQLException {
        String sql = "SELECT order_id, customer_id, order_time, order_status, goods_amount, " +
                "discount_rate_snapshot, payable_amount, shipping_address_snapshot, payment_time " +
                "FROM sales_order WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SalesOrder o = new SalesOrder();
                    o.setOrderId(rs.getLong("order_id"));
                    o.setCustomerId(rs.getLong("customer_id"));
                    Timestamp orderTime = rs.getTimestamp("order_time");
                    if (orderTime != null) {
                        o.setOrderTime(orderTime.toLocalDateTime());
                    }
                    Timestamp payTime = rs.getTimestamp("payment_time");
                    if (payTime != null) {
                        o.setPaymentTime(payTime.toLocalDateTime());
                    }
                    o.setOrderStatus(rs.getString("order_status"));
                    o.setGoodsAmount(rs.getBigDecimal("goods_amount"));
                    o.setDiscountRateSnapshot(rs.getBigDecimal("discount_rate_snapshot"));
                    o.setPayableAmount(rs.getBigDecimal("payable_amount"));
                    o.setShippingAddressSnapshot(rs.getString("shipping_address_snapshot"));
                    return o;
                }
                return null;
            }
        }
    }

    /**
     * 更新订单状态与支付时间（用于付款后标记为待发货）。
     */
    public int updateStatusAndPaymentTime(long orderId, String newStatus, LocalDateTime paymentTime) throws SQLException {
        String sql = "UPDATE sales_order SET order_status = ?, payment_time = ? WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setTimestamp(2, paymentTime != null ? Timestamp.valueOf(paymentTime) : null);
            ps.setLong(3, orderId);
            return ps.executeUpdate();
        }
    }

    /**
     * 更新订单状态与发货时间（用于发货后标记为已发货）。
     */
    public int updateStatusAndDeliveryTime(long orderId, String newStatus, LocalDateTime deliveryTime) throws SQLException {
        String sql = "UPDATE sales_order SET order_status = ?, delivery_time = ? WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setTimestamp(2, deliveryTime != null ? Timestamp.valueOf(deliveryTime) : null);
            ps.setLong(3, orderId);
            return ps.executeUpdate();
        }
    }

    /**
     * 简单查询：根据 order_id 查询该订单的所有明细。
     */
    public List<SalesOrderItem> findItemsByOrderId(long orderId) throws SQLException {
        String sql = "SELECT order_item_id, order_id, book_id, quantity, shipped_quantity, received_quantity, unit_price, sub_amount, item_status " +
                "FROM sales_order_item WHERE order_id = ?";
        List<SalesOrderItem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesOrderItem item = new SalesOrderItem();
                    item.setOrderItemId(rs.getLong("order_item_id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setBookId(rs.getString("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setShippedQuantity(rs.getInt("shipped_quantity"));
                    item.setReceivedQuantity(rs.getInt("received_quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubAmount(rs.getBigDecimal("sub_amount"));
                    item.setItemStatus(rs.getString("item_status"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * 增量更新订单明细的发货/收货进度，并可同时更新状态。
     */
    public int updateItemProgress(long orderItemId, int addShipped, int addReceived, String newStatus) throws SQLException {
        String sql = "UPDATE sales_order_item SET shipped_quantity = shipped_quantity + ?, " +
                "received_quantity = received_quantity + ?, item_status = ? WHERE order_item_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addShipped);
            ps.setInt(2, addReceived);
            ps.setString(3, newStatus);
            ps.setLong(4, orderItemId);
            return ps.executeUpdate();
        }
    }

    /**
     * 查询所有订单。
     */
    public List<SalesOrder> findAll() throws SQLException {
        String sql = "SELECT order_id, customer_id, order_time, order_status, goods_amount, " +
                "discount_rate_snapshot, payable_amount, shipping_address_snapshot, payment_time " +
                "FROM sales_order ORDER BY order_id DESC";
        List<SalesOrder> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOrderRow(rs));
            }
        }
        return list;
    }

    /**
     * 按状态查询订单。
     */
    public List<SalesOrder> findByStatus(String status) throws SQLException {
        String sql = "SELECT order_id, customer_id, order_time, order_status, goods_amount, " +
                "discount_rate_snapshot, payable_amount, shipping_address_snapshot, payment_time " +
                "FROM sales_order WHERE order_status = ? ORDER BY order_id DESC";
        List<SalesOrder> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 按客户查询其所有订单，按时间倒序。
     */
    public List<SalesOrder> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT order_id, customer_id, order_time, order_status, goods_amount, " +
                "discount_rate_snapshot, payable_amount, shipping_address_snapshot, payment_time " +
                "FROM sales_order WHERE customer_id = ? ORDER BY order_id DESC";
        List<SalesOrder> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderRow(rs));
                }
            }
        }
        return list;
    }

    private SalesOrder mapOrderRow(ResultSet rs) throws SQLException {
        SalesOrder o = new SalesOrder();
        o.setOrderId(rs.getLong("order_id"));
        o.setCustomerId(rs.getLong("customer_id"));
        Timestamp orderTime = rs.getTimestamp("order_time");
        if (orderTime != null) {
            o.setOrderTime(orderTime.toLocalDateTime());
        }
        Timestamp payTime = rs.getTimestamp("payment_time");
        if (payTime != null) {
            o.setPaymentTime(payTime.toLocalDateTime());
        }
        o.setOrderStatus(rs.getString("order_status"));
        o.setGoodsAmount(rs.getBigDecimal("goods_amount"));
        o.setDiscountRateSnapshot(rs.getBigDecimal("discount_rate_snapshot"));
        o.setPayableAmount(rs.getBigDecimal("payable_amount"));
        o.setShippingAddressSnapshot(rs.getString("shipping_address_snapshot"));
        return o;
    }
}



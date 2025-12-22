package com.bookstore.dao;

import com.bookstore.model.Shipment;
import com.bookstore.model.ShipmentItem;
import com.bookstore.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 发货单及发货明细 DAO。
 */
public class ShipmentDao {

    public long createShipment(Shipment shipment, List<ShipmentItem> items) throws SQLException {
        String insertShipmentSql = "INSERT INTO shipment (order_id, ship_time, carrier, tracking_number, shipment_status, operator) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO shipment_item (shipment_id, order_item_id, ship_quantity, receive_status, received_quantity) " +
                "VALUES (?, ?, ?, 'PENDING', 0)";

        long shipmentId = -1;
        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement psShip = conn.prepareStatement(insertShipmentSql, Statement.RETURN_GENERATED_KEYS)) {
                    psShip.setLong(1, shipment.getOrderId());
                    psShip.setTimestamp(2, Timestamp.valueOf(
                            shipment.getShipTime() != null ? shipment.getShipTime() : LocalDateTime.now()));
                    psShip.setString(3, shipment.getCarrier());
                    psShip.setString(4, shipment.getTrackingNumber());
                    psShip.setString(5, shipment.getShipmentStatus());
                    psShip.setString(6, shipment.getOperator());
                    psShip.executeUpdate();

                    try (ResultSet keys = psShip.getGeneratedKeys()) {
                        if (keys.next()) {
                            shipmentId = keys.getLong(1);
                            shipment.setShipmentId(shipmentId);

                            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                                for (ShipmentItem item : items) {
                                    item.setShipmentId(shipmentId);
                                    psItem.setLong(1, item.getShipmentId());
                                    psItem.setLong(2, item.getOrderItemId());
                                    psItem.setInt(3, item.getShipQuantity());
                                    psItem.addBatch();
                                }
                                psItem.executeBatch();
                            }
                        } else {
                            throw new SQLException("创建发货单失败，未获取到主键。");
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
        return shipmentId;
    }

    public List<Shipment> findByOrderId(long orderId) throws SQLException {
        String sql = "SELECT shipment_id, order_id, ship_time, carrier, tracking_number, shipment_status, operator " +
                "FROM shipment WHERE order_id = ?";
        List<Shipment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Shipment s = new Shipment();
                    s.setShipmentId(rs.getLong("shipment_id"));
                    s.setOrderId(rs.getLong("order_id"));
                    Timestamp ts = rs.getTimestamp("ship_time");
                    if (ts != null) {
                        s.setShipTime(ts.toLocalDateTime());
                    }
                    s.setCarrier(rs.getString("carrier"));
                    s.setTrackingNumber(rs.getString("tracking_number"));
                    s.setShipmentStatus(rs.getString("shipment_status"));
                    s.setOperator(rs.getString("operator"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    public List<ShipmentItem> findItemsByShipmentId(long shipmentId) throws SQLException {
        String sql = "SELECT shipment_item_id, shipment_id, order_item_id, ship_quantity, receive_status, received_quantity, received_time " +
                "FROM shipment_item WHERE shipment_id = ?";
        List<ShipmentItem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShipmentItem item = new ShipmentItem();
                    item.setShipmentItemId(rs.getLong("shipment_item_id"));
                    item.setShipmentId(rs.getLong("shipment_id"));
                    item.setOrderItemId(rs.getLong("order_item_id"));
                    item.setShipQuantity(rs.getInt("ship_quantity"));
                    item.setReceiveStatus(rs.getString("receive_status"));
                    item.setReceivedQuantity(rs.getInt("received_quantity"));
                    Timestamp rt = rs.getTimestamp("received_time");
                    if (rt != null) {
                        item.setReceivedTime(rt.toLocalDateTime());
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * 查询某订单明细下尚未全部收货的发货明细（按创建顺序）。
     */
    public List<ShipmentItem> findPendingByOrderItem(long orderItemId) throws SQLException {
        String sql = "SELECT shipment_item_id, shipment_id, order_item_id, ship_quantity, receive_status, received_quantity, received_time " +
                "FROM shipment_item WHERE order_item_id = ? AND receive_status = 'PENDING' ORDER BY shipment_item_id ASC";
        List<ShipmentItem> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShipmentItem item = new ShipmentItem();
                    item.setShipmentItemId(rs.getLong("shipment_item_id"));
                    item.setShipmentId(rs.getLong("shipment_id"));
                    item.setOrderItemId(rs.getLong("order_item_id"));
                    item.setShipQuantity(rs.getInt("ship_quantity"));
                    item.setReceiveStatus(rs.getString("receive_status"));
                    item.setReceivedQuantity(rs.getInt("received_quantity"));
                    Timestamp rt = rs.getTimestamp("received_time");
                    if (rt != null) {
                        item.setReceivedTime(rt.toLocalDateTime());
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * 更新发货明细的收货进度。
     */
    public int updateReceiveProgress(long shipmentItemId, int addReceived, boolean finished) throws SQLException {
        String sql = "UPDATE shipment_item SET received_quantity = received_quantity + ?, " +
                "receive_status = ?, received_time = ? WHERE shipment_item_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addReceived);
            ps.setString(2, finished ? "RECEIVED" : "PENDING");
            ps.setTimestamp(3, finished ? Timestamp.valueOf(java.time.LocalDateTime.now()) : null);
            ps.setLong(4, shipmentItemId);
            return ps.executeUpdate();
        }
    }

    /**
     * 汇总某订单明细的累计发货数量（用于兜底校验/显示）。
     */
    public int sumShippedQuantityByOrderItem(long orderItemId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ship_quantity),0) AS total FROM shipment_item WHERE order_item_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        }
    }
}



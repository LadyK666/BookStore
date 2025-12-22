package com.bookstore.service;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * 发货业务服务
 * 整合发货单创建、库存扣减、订单状态更新
 */
public class ShipmentService {

    private final SalesOrderDao salesOrderDao = new SalesOrderDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final ShipmentDao shipmentDao = new ShipmentDao();
    private final OutOfStockRecordDao outOfStockRecordDao = new OutOfStockRecordDao();
    private final CustomerNotificationDao customerNotificationDao = new CustomerNotificationDao();

    /**
     * 执行发货：
     * 1. 校验订单状态必须为 PENDING_SHIPMENT
     * 2. 校验库存是否充足
     * 3. 创建发货单与发货明细
     * 4. 扣减库存
     * 5. 更新订单状态为 SHIPPED
     * 
     * @param orderId 订单ID
     * @param carrier 物流公司
     * @param trackingNumber 运单号
     * @param operator 操作员
     * @return 生成的发货单ID
     */
    public long shipOrder(long orderId, String carrier, String trackingNumber, String operator) throws SQLException {
        // 1. 查询订单
        SalesOrder order = salesOrderDao.findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        
        // 2. 根据信用等级判断是否允许先发货后付款
        CustomerDao customerDao = new CustomerDao();
        CreditLevelDao creditLevelDao = new CreditLevelDao();
        Customer customer = customerDao.findById(order.getCustomerId());
        if (customer == null) {
            throw new IllegalStateException("关联客户不存在");
        }
        CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
        if (level == null) {
            throw new IllegalStateException("客户信用等级不存在");
        }
        
        // 允许发货的状态：待发货（已付款）或待付款（三级及以上可先发货）
        boolean canShipWithoutPayment = level.isAllowOverdraft() && 
                ("PENDING_PAYMENT".equals(order.getOrderStatus()) || "OUT_OF_STOCK_PENDING".equals(order.getOrderStatus()));
        boolean isPaid = "PENDING_SHIPMENT".equals(order.getOrderStatus());
        
        if (!isPaid && !canShipWithoutPayment) {
            throw new IllegalStateException("订单状态不允许发货。当前状态: " + order.getOrderStatus() + 
                    "，您的信用等级（" + level.getLevelName() + "）必须先付款后发货");
        }

        // 2. 查询订单明细
        List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
        if (items.isEmpty()) {
            throw new IllegalStateException("订单明细为空，无法发货");
        }

        // 3. 校验库存是否充足（整单发货场景）
        for (SalesOrderItem item : items) {
            int currentQty = inventoryDao.getQuantity(item.getBookId());
            if (currentQty < item.getQuantity()) {
                throw new IllegalStateException(
                    String.format("库存不足：书号 %s 当前库存 %d，需要 %d",
                        item.getBookId(), currentQty, item.getQuantity())
                );
            }
        }

        // 4. 创建发货单（整单发货）
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setShipTime(LocalDateTime.now());
        shipment.setCarrier(carrier);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setShipmentStatus("SHIPPED");
        shipment.setOperator(operator);

        List<ShipmentItem> shipmentItems = new ArrayList<>();
        for (SalesOrderItem item : items) {
            ShipmentItem si = new ShipmentItem();
            si.setOrderItemId(item.getOrderItemId());
            si.setShipQuantity(item.getQuantity());  // 全部发货
            shipmentItems.add(si);
        }

        long shipmentId = createShipmentWithInventoryUpdate(shipment, shipmentItems, items);

        System.out.println("发货成功，shipment_id = " + shipmentId);
        return shipmentId;
    }

    /**
     * 分次发货：管理员为每个订单明细指定本次要发出的数量。
     * 仅处理 shipQuantity > 0 的条目。
     */
    public long shipOrderPartially(long orderId, List<ShipmentItem> toShip, String carrier,
                                   String trackingNumber, String operator) throws SQLException {
        SalesOrder order = salesOrderDao.findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        // 允许发货的状态：待发货/待付款且允许赊销/缺货待确认通过后
        if (!"PENDING_SHIPMENT".equals(order.getOrderStatus())
                && !"PENDING_PAYMENT".equals(order.getOrderStatus())
                && !"OUT_OF_STOCK_PENDING".equals(order.getOrderStatus())
                && !"DELIVERING".equals(order.getOrderStatus())) {
            throw new IllegalStateException("当前状态不支持发货: " + order.getOrderStatus());
        }

        // 信用等级校验与整单发货一致
        CustomerDao customerDao = new CustomerDao();
        CreditLevelDao creditLevelDao = new CreditLevelDao();
        Customer customer = customerDao.findById(order.getCustomerId());
        if (customer == null) {
            throw new IllegalStateException("关联客户不存在");
        }
        CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
        if (level == null) {
            throw new IllegalStateException("客户信用等级不存在");
        }
        boolean canShipWithoutPayment = level.isAllowOverdraft() &&
                ("PENDING_PAYMENT".equals(order.getOrderStatus()) || "OUT_OF_STOCK_PENDING".equals(order.getOrderStatus()));
        boolean isPaidOrDelivering = "PENDING_SHIPMENT".equals(order.getOrderStatus()) || "DELIVERING".equals(order.getOrderStatus());
        if (!isPaidOrDelivering && !canShipWithoutPayment) {
            throw new IllegalStateException("订单状态不允许发货。当前状态: " + order.getOrderStatus());
        }

        // 读取订单明细，计算剩余可发数量
        List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
        Map<Long, SalesOrderItem> itemMap = new HashMap<>();
        for (SalesOrderItem it : items) {
            itemMap.put(it.getOrderItemId(), it);
        }

        Map<String, Integer> needByBook = new HashMap<>();
        List<ShipmentItem> validShipItems = new ArrayList<>();
        for (ShipmentItem si : toShip) {
            if (si.getShipQuantity() == null || si.getShipQuantity() <= 0) continue;
            SalesOrderItem oi = itemMap.get(si.getOrderItemId());
            if (oi == null) {
                throw new IllegalArgumentException("无效的订单明细ID: " + si.getOrderItemId());
            }
            int remaining = oi.getQuantity() - (oi.getShippedQuantity() == null ? 0 : oi.getShippedQuantity());
            if (si.getShipQuantity() > remaining) {
                throw new IllegalArgumentException("发货数量超出剩余待发货量, 书号=" + oi.getBookId());
            }
            validShipItems.add(si);
            needByBook.merge(oi.getBookId(), si.getShipQuantity(), Integer::sum);
        }

        if (validShipItems.isEmpty()) {
            throw new IllegalStateException("没有需要发货的图书数量");
        }

        // 校验库存（按书号汇总）
        for (Map.Entry<String, Integer> entry : needByBook.entrySet()) {
            int currentQty = inventoryDao.getQuantity(entry.getKey());
            if (currentQty < entry.getValue()) {
                throw new IllegalStateException(
                        String.format("库存不足：书号 %s 当前库存 %d，本次发货需要 %d",
                                entry.getKey(), currentQty, entry.getValue())
                );
            }
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setShipTime(LocalDateTime.now());
        shipment.setCarrier(carrier);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setShipmentStatus("SHIPPED");
        shipment.setOperator(operator);

        long sid = createShipmentWithInventoryUpdate(shipment, validShipItems, items);

        // 生成分次发货通知给顾客
        try {
            CustomerNotification n = new CustomerNotification();
            n.setCustomerId(order.getCustomerId());
            n.setOrderId(orderId);
            n.setType("PARTIAL_SHIPMENT");
            n.setTitle("订单部分发货通知");

            StringBuilder shippedMsg = new StringBuilder();
            StringBuilder unshippedMsg = new StringBuilder();
            for (SalesOrderItem it : items) {
                int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                int qty = it.getQuantity() != null ? it.getQuantity() : 0;
                if (shipped > 0) {
                    shippedMsg.append(it.getBookId())
                            .append(" 已发 ").append(shipped).append(" 本；");
                }
                if (shipped < qty) {
                    unshippedMsg.append(it.getBookId())
                            .append(" 未发 ").append(qty - shipped).append(" 本；");
                }
            }
            StringBuilder content = new StringBuilder();
            content.append("您的订单（").append(orderId).append("）已部分发货。");
            if (shippedMsg.length() > 0) {
                content.append("已发货：").append(shippedMsg);
            }
            if (unshippedMsg.length() > 0) {
                content.append(" 未发货：").append(unshippedMsg);
            }
            n.setContent(content.toString());
            n.setReadFlag(false);
            customerNotificationDao.insert(n);
        } catch (Exception ignore) {
            // 通知失败不影响主流程
        }

        System.out.println("分次发货成功，shipment_id = " + sid);
        return sid;
    }

    /**
     * 事务内完成：创建发货单、扣减库存、更新订单状态
     */
    private long createShipmentWithInventoryUpdate(Shipment shipment, List<ShipmentItem> shipmentItems,
                                                    List<SalesOrderItem> orderItems) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 创建发货单（使用独立连接的方法，这里简化处理）
            long shipmentId = shipmentDao.createShipment(shipment, shipmentItems);

            // 扣减库存
            Map<Long, SalesOrderItem> itemMap = new HashMap<>();
            for (SalesOrderItem oi : orderItems) {
                itemMap.put(oi.getOrderItemId(), oi);
            }
            Map<String, Integer> deductByBook = new HashMap<>();
            for (ShipmentItem si : shipmentItems) {
                SalesOrderItem oi = itemMap.get(si.getOrderItemId());
                if (oi == null) continue;
                deductByBook.merge(oi.getBookId(), si.getShipQuantity(), Integer::sum);
            }
            for (Map.Entry<String, Integer> entry : deductByBook.entrySet()) {
                String bookId = entry.getKey();
                int need = entry.getValue();
                int affected = inventoryDao.decreaseQuantity(bookId, need);
                if (affected == 0) {
                    throw new SQLException("库存扣减失败，可能库存不足: " + bookId);
                }
                System.out.printf("  书号 %s 库存扣减 %d%n", bookId, need);

                // 发货后如库存跌破安全库存，则自动生成/累加缺书记录（LOW_STOCK）
                int qty = inventoryDao.getQuantity(bookId);
                int safety = inventoryDao.getSafetyStock(bookId);
                if (safety > 0 && qty < safety) {
                    OutOfStockRecord record = new OutOfStockRecord();
                    record.setBookId(bookId);
                    record.setRequiredQuantity(safety - qty);
                    record.setRecordDate(java.time.LocalDate.now());
                    record.setSource("LOW_STOCK");
                    record.setStatus("PENDING");
                    record.setPriority(1);
                    outOfStockRecordDao.insert(record);
                }
            }

            // 更新订单明细的发货进度
            for (ShipmentItem si : shipmentItems) {
                SalesOrderItem oi = itemMap.get(si.getOrderItemId());
                if (oi == null) continue;
                int shippedSoFar = oi.getShippedQuantity() == null ? 0 : oi.getShippedQuantity();
                int newTotal = shippedSoFar + si.getShipQuantity();
                String newStatus = newTotal >= oi.getQuantity() ? "SHIPPED" : "PART_SHIPPED";
                salesOrderDao.updateItemProgress(oi.getOrderItemId(), si.getShipQuantity(), 0, newStatus);
            }

            // 订单状态：进入正在运输 DELIVERING，并记录发货时间
            salesOrderDao.updateStatusAndDeliveryTime(shipment.getOrderId(), "DELIVERING", LocalDateTime.now());

            conn.commit();
            return shipmentId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * 顾客确认收货（可分次收货）。receiveMap: orderItemId -> 本次确认的数量。
     * 仅允许确认已发货但未收货的数量。
     */
    public void confirmReceipt(long orderId, Map<Long, Integer> receiveMap) throws SQLException {
        if (receiveMap == null || receiveMap.isEmpty()) {
            throw new IllegalArgumentException("收货数量不能为空");
        }

        SalesOrder order = salesOrderDao.findOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("订单不存在: " + orderId);
        if (!"DELIVERING".equals(order.getOrderStatus()) && !"SHIPPED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("当前状态不允许确认收货: " + order.getOrderStatus());
        }

        List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
        Map<Long, SalesOrderItem> itemMap = new HashMap<>();
        for (SalesOrderItem it : items) itemMap.put(it.getOrderItemId(), it);

        // 前置校验
        for (Map.Entry<Long, Integer> entry : receiveMap.entrySet()) {
            SalesOrderItem oi = itemMap.get(entry.getKey());
            if (oi == null) throw new IllegalArgumentException("无效的订单明细ID: " + entry.getKey());
            int shipped = oi.getShippedQuantity() == null ? 0 : oi.getShippedQuantity();
            // 兜底：若历史数据未写入 shipped_quantity，则用发货明细汇总
            if (shipped == 0) {
                shipped = shipmentDao.sumShippedQuantityByOrderItem(oi.getOrderItemId());
            }
            int received = oi.getReceivedQuantity() == null ? 0 : oi.getReceivedQuantity();
            int remain = shipped - received;
            if (remain <= 0) {
                throw new IllegalStateException("该明细已全部确认收货，bookId=" + oi.getBookId());
            }
            if (entry.getValue() <= 0 || entry.getValue() > remain) {
                throw new IllegalArgumentException("收货数量非法，bookId=" + oi.getBookId() + " 剩余可收货：" + remain);
            }
        }

        // 按发货顺序将收货数量分摊到 shipment_item
        for (Map.Entry<Long, Integer> entry : receiveMap.entrySet()) {
            long orderItemId = entry.getKey();
            int needReceive = entry.getValue();
            List<ShipmentItem> pendingList = shipmentDao.findPendingByOrderItem(orderItemId);
            for (ShipmentItem si : pendingList) {
                int shipped = si.getShipQuantity();
                int received = si.getReceivedQuantity() == null ? 0 : si.getReceivedQuantity();
                int remain = shipped - received;
                if (remain <= 0) continue;
                int take = Math.min(remain, needReceive);
                boolean finishedThisLine = (remain == take);
                shipmentDao.updateReceiveProgress(si.getShipmentItemId(), take, finishedThisLine);
                needReceive -= take;
                if (needReceive == 0) break;
            }
            // 更新订单明细的收货进度
            SalesOrderItem oi = itemMap.get(orderItemId);
            int newReceived = (oi.getReceivedQuantity() == null ? 0 : oi.getReceivedQuantity()) + entry.getValue();
            String status = newReceived >= oi.getQuantity() ? "RECEIVED" : "PART_SHIPPED";
            salesOrderDao.updateItemProgress(orderItemId, 0, entry.getValue(), status);
            oi.setReceivedQuantity(newReceived);
        }

        // 判断整单是否收货完成
        boolean allReceived = true;
        for (SalesOrderItem oi : itemMap.values()) {
            int received = oi.getReceivedQuantity() == null ? 0 : oi.getReceivedQuantity();
            if (received < oi.getQuantity()) {
                allReceived = false;
                break;
            }
        }
        if (allReceived) {
            salesOrderDao.updateStatusAndDeliveryTime(orderId, "COMPLETED", order.getDeliveryTime());
        } else {
            salesOrderDao.updateStatusAndDeliveryTime(orderId, "DELIVERING", order.getDeliveryTime());
        }
    }
}


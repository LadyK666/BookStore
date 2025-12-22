package com.bookstore;

import com.bookstore.dao.SalesOrderDao;
import com.bookstore.dao.ShipmentDao;
import com.bookstore.model.SalesOrderItem;
import com.bookstore.model.Shipment;
import com.bookstore.model.ShipmentItem;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试发货流程（简化版）：
 *  前置：已经有 order_id=1 且状态为 PENDING_SHIPMENT，并且有对应的订单明细。
 *
 *  步骤：
 *   1）查询该订单的全部明细；
 *   2）对所有明细一次性全部发货（不做分次与库存校验，仅演示数据流）；
 *   3）查询并打印发货单与发货明细。
 */
public class TestShipment {

    public static void main(String[] args) {
        long orderId = 1L;
        SalesOrderDao orderDao = new SalesOrderDao();
        ShipmentDao shipmentDao = new ShipmentDao();

        try {
            List<SalesOrderItem> orderItems = orderDao.findItemsByOrderId(orderId);
            if (orderItems.isEmpty()) {
                System.out.println("订单 " + orderId + " 没有明细，请先运行 TestOrder 生成订单。");
                return;
            }

            Shipment shipment = new Shipment();
            shipment.setOrderId(orderId);
            shipment.setShipTime(LocalDateTime.now());
            shipment.setCarrier("顺丰速运");
            shipment.setTrackingNumber("SF123456789");
            shipment.setShipmentStatus("SHIPPED");
            shipment.setOperator("admin");

            List<ShipmentItem> items = new ArrayList<>();
            for (SalesOrderItem oi : orderItems) {
                ShipmentItem si = new ShipmentItem();
                si.setOrderItemId(oi.getOrderItemId());
                si.setShipQuantity(oi.getQuantity()); // 简化：直接一次性全发
                items.add(si);
            }

            shipmentDao.createShipment(shipment, items);
            System.out.println("创建发货单成功，shipment_id = " + shipment.getShipmentId());

            System.out.println("根据订单查询发货单：");
            List<Shipment> shipments = shipmentDao.findByOrderId(orderId);
            for (Shipment s : shipments) {
                System.out.println(s);
                List<ShipmentItem> sis = shipmentDao.findItemsByShipmentId(s.getShipmentId());
                System.out.println("  对应的发货明细：");
                for (ShipmentItem si : sis) {
                    System.out.println("    " + si);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



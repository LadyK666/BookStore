package com.bookstore;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.service.OrderService;
import com.bookstore.service.ShipmentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试完整流程：下单 → 付款 → 发货（含库存扣减）
 */
public class TestShipmentWithInventory {
    public static void main(String[] args) throws SQLException {
        BookDao bookDao = new BookDao();
        CustomerDao customerDao = new CustomerDao();
        CreditLevelDao creditLevelDao = new CreditLevelDao();
        SalesOrderDao salesOrderDao = new SalesOrderDao();
        InventoryDao inventoryDao = new InventoryDao();
        OrderService orderService = new OrderService();
        ShipmentService shipmentService = new ShipmentService();

        // ========== 步骤 1：查看初始库存 ==========
        System.out.println("========== 步骤 1：查看初始库存 ==========");
        int b001Before = inventoryDao.getQuantity("B001");
        int b002Before = inventoryDao.getQuantity("B002");
        System.out.println("B001 库存：" + b001Before);
        System.out.println("B002 库存：" + b002Before);

        // ========== 步骤 2：创建新订单 ==========
        System.out.println("\n========== 步骤 2：创建新订单 ==========");
        // 使用客户 lisi（customer_id=2，信用等级4）
        Customer customer = customerDao.findById(2L);
        if (customer == null) {
            System.out.println("客户 lisi 不存在，请先执行 TestCustomer 或检查测试数据");
            return;
        }
        CreditLevel creditLevel = creditLevelDao.findById(customer.getCreditLevelId());
        BigDecimal discountRate = creditLevel.getDiscountRate();
        System.out.println("客户：" + customer.getRealName() + "，信用等级：" + creditLevel.getLevelName() + "，折扣率：" + discountRate);

        // 获取书籍价格
        Book b001 = bookDao.findById("B001");
        Book b002 = bookDao.findById("B002");

        // 计算折扣价
        BigDecimal b001UnitPrice = b001.getPrice().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal b002UnitPrice = b002.getPrice().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

        // 订购数量
        int qty1 = 2;  // B001 买 2 本
        int qty2 = 1;  // B002 买 1 本

        BigDecimal sub1 = b001UnitPrice.multiply(BigDecimal.valueOf(qty1));
        BigDecimal sub2 = b002UnitPrice.multiply(BigDecimal.valueOf(qty2));
        BigDecimal totalPayable = sub1.add(sub2);

        System.out.println("订购：B001 x " + qty1 + "，单价 " + b001UnitPrice + "，小计 " + sub1);
        System.out.println("订购：B002 x " + qty2 + "，单价 " + b002UnitPrice + "，小计 " + sub2);
        System.out.println("应付金额：" + totalPayable);

        // 创建订单
        SalesOrder order = new SalesOrder();
        order.setCustomerId(customer.getCustomerId());
        order.setOrderTime(LocalDateTime.now());
        order.setOrderStatus("PENDING_PAYMENT");
        order.setGoodsAmount(totalPayable);
        order.setDiscountRateSnapshot(discountRate);
        order.setPayableAmount(totalPayable);
        order.setShippingAddressSnapshot(customer.getRealName() + ", 测试地址, " + customer.getMobilePhone());

        SalesOrderItem item1 = new SalesOrderItem();
        item1.setBookId("B001");
        item1.setQuantity(qty1);
        item1.setUnitPrice(b001UnitPrice);
        item1.setSubAmount(sub1);
        item1.setItemStatus("ORDERED");

        SalesOrderItem item2 = new SalesOrderItem();
        item2.setBookId("B002");
        item2.setQuantity(qty2);
        item2.setUnitPrice(b002UnitPrice);
        item2.setSubAmount(sub2);
        item2.setItemStatus("ORDERED");

        List<SalesOrderItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        salesOrderDao.createOrder(order, items);
        long orderId = order.getOrderId();
        System.out.println("订单创建成功，order_id = " + orderId);

        // ========== 步骤 3：付款 ==========
        System.out.println("\n========== 步骤 3：付款 ==========");
        BigDecimal balanceBefore = customerDao.findById(customer.getCustomerId()).getAccountBalance();
        System.out.println("付款前余额：" + balanceBefore);

        orderService.payOrder(orderId);

        SalesOrder orderAfterPay = salesOrderDao.findOrderById(orderId);
        BigDecimal balanceAfterPay = customerDao.findById(customer.getCustomerId()).getAccountBalance();
        System.out.println("付款后订单状态：" + orderAfterPay.getOrderStatus());
        System.out.println("付款后余额：" + balanceAfterPay);

        // ========== 步骤 4：发货（含库存扣减） ==========
        System.out.println("\n========== 步骤 4：发货（含库存扣减） ==========");
        long shipmentId = shipmentService.shipOrder(orderId, "中通快递", "ZTO" + System.currentTimeMillis(), "发货员小李");

        // ========== 步骤 5：验证结果 ==========
        System.out.println("\n========== 步骤 5：验证结果 ==========");
        System.out.println("生成的发货单ID：" + shipmentId);
        SalesOrder orderAfterShip = salesOrderDao.findOrderById(orderId);
        System.out.println("发货后订单状态：" + orderAfterShip.getOrderStatus());

        int b001After = inventoryDao.getQuantity("B001");
        int b002After = inventoryDao.getQuantity("B002");
        System.out.println("B001 库存：" + b001After + "（减少了 " + (b001Before - b001After) + "）");
        System.out.println("B002 库存：" + b002After + "（减少了 " + (b002Before - b002After) + "）");

        // 查询发货单
        ShipmentDao shipmentDao = new ShipmentDao();
        List<Shipment> shipments = shipmentDao.findByOrderId(orderId);
        System.out.println("该订单的发货单：");
        for (Shipment s : shipments) {
            System.out.println("  " + s);
        }
    }
}


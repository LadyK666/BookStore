package com.bookstore;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CreditLevelDao;
import com.bookstore.dao.CustomerDao;
import com.bookstore.dao.SalesOrderDao;
import com.bookstore.model.Book;
import com.bookstore.model.CreditLevel;
import com.bookstore.model.Customer;
import com.bookstore.model.SalesOrder;
import com.bookstore.model.SalesOrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试“创建订单 + 按信用等级折扣计算 + 保存到数据库”的简单流程。
 *
 * 场景：
 *  - 使用已存在的客户 zhangsan（信用等级 3，折扣率在 credit_level 表中）
 *  - 购买书籍 B001 和 B002，各若干本
 */
public class TestOrder {

    public static void main(String[] args) {
        CustomerDao customerDao = new CustomerDao();
        CreditLevelDao creditLevelDao = new CreditLevelDao();
        BookDao bookDao = new BookDao();
        SalesOrderDao orderDao = new SalesOrderDao();

        try {
            // 1. 找到客户 zhangsan
            Customer customer = customerDao.findByUsername("zhangsan");
            if (customer == null) {
                System.out.println("未找到用户名为 zhangsan 的客户，请确认测试数据已插入。");
                return;
            }

            // 2. 查询其信用等级，获得折扣率
            CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
            if (level == null) {
                System.out.println("未找到对应的信用等级记录，credit_level_id=" + customer.getCreditLevelId());
                return;
            }
            BigDecimal discountRate = level.getDiscountRate(); // 例如 0.85
            System.out.println("客户 " + customer.getUsername() + " 的信用等级为：" +
                    level.getLevelName() + "，折扣率=" + discountRate);

            // 3. 查询要购买的图书
            Book b1 = bookDao.findById("B001");
            Book b2 = bookDao.findById("B002");
            if (b1 == null || b2 == null) {
                System.out.println("测试书籍 B001 或 B002 不存在，请检查 book 表测试数据。");
                return;
            }

            // 4. 构造订单明细：B001 买 1 本，B002 买 2 本
            List<SalesOrderItem> items = new ArrayList<>();

            SalesOrderItem item1 = new SalesOrderItem();
            item1.setBookId(b1.getBookId());
            item1.setQuantity(1);
            BigDecimal unitPrice1 = b1.getPrice().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            item1.setUnitPrice(unitPrice1);
            item1.setSubAmount(unitPrice1.multiply(BigDecimal.valueOf(item1.getQuantity())));
            item1.setItemStatus("ORDERED");
            items.add(item1);

            SalesOrderItem item2 = new SalesOrderItem();
            item2.setBookId(b2.getBookId());
            item2.setQuantity(2);
            BigDecimal unitPrice2 = b2.getPrice().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            item2.setUnitPrice(unitPrice2);
            item2.setSubAmount(unitPrice2.multiply(BigDecimal.valueOf(item2.getQuantity())));
            item2.setItemStatus("ORDERED");
            items.add(item2);

            // 5. 汇总金额
            BigDecimal goodsAmount = BigDecimal.ZERO;
            for (SalesOrderItem it : items) {
                goodsAmount = goodsAmount.add(it.getSubAmount());
            }

            // 6. 构造订单主记录
            SalesOrder order = new SalesOrder();
            order.setCustomerId(customer.getCustomerId());
            order.setOrderTime(LocalDateTime.now());
            order.setOrderStatus("PENDING_PAYMENT"); // 当前阶段先不做扣款/发货
            order.setGoodsAmount(goodsAmount);
            order.setDiscountRateSnapshot(discountRate);
            order.setPayableAmount(goodsAmount); // 已经是打完折的单价汇总
            order.setShippingAddressSnapshot("张三, 北京市海淀区 某路某号, 13800000001");
            order.setCustomerNote("测试下单");

            // 7. 调用 DAO 在一个事务中创建订单和明细
            orderDao.createOrder(order, items);
            System.out.println("下单成功，生成的 order_id = " + order.getOrderId());

            // 8. 再从数据库查回订单及其明细打印验证
            SalesOrder saved = orderDao.findOrderById(order.getOrderId());
            List<SalesOrderItem> savedItems = orderDao.findItemsByOrderId(order.getOrderId());

            System.out.println("从数据库查询到的订单主表记录：");
            System.out.println(saved);
            System.out.println("对应的订单明细：");
            for (SalesOrderItem it : savedItems) {
                System.out.println(it);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



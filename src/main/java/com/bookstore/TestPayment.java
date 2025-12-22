package com.bookstore;

import com.bookstore.dao.CustomerDao;
import com.bookstore.dao.SalesOrderDao;
import com.bookstore.model.Customer;
import com.bookstore.model.SalesOrder;
import com.bookstore.service.OrderService;

/**
 * 测试订单付款与信用校验：
 *  前置：已经通过 TestOrder 生成了一个 order_id=1 的订单，客户为 zhangsan。
 *
 *  步骤：
 *   1）打印客户当前余额与订单状态；
 *   2）调用 OrderService.payOrder(1) 执行付款；
 *   3）再次打印客户余额与订单状态，观察变化。
 */
public class TestPayment {

    public static void main(String[] args) {
        long orderId = 1L; // 默认测试第一个订单

        CustomerDao customerDao = new CustomerDao();
        SalesOrderDao orderDao = new SalesOrderDao();
        OrderService orderService = new OrderService();

        try {
            SalesOrder orderBefore = orderDao.findOrderById(orderId);
            if (orderBefore == null) {
                System.out.println("未找到订单 order_id=" + orderId + "，请先运行 TestOrder 生成订单。");
                return;
            }

            Customer customerBefore = customerDao.findById(orderBefore.getCustomerId());
            System.out.println("付款前：");
            System.out.println("  订单状态 = " + orderBefore.getOrderStatus() +
                    "，应付金额 = " + orderBefore.getPayableAmount());
            System.out.println("  客户余额 = " + customerBefore.getAccountBalance());

            try {
                orderService.payOrder(orderId);
                System.out.println("付款成功！");
            } catch (IllegalStateException ex) {
                System.out.println("付款失败：" + ex.getMessage());
                return;
            }

            SalesOrder orderAfter = orderDao.findOrderById(orderId);
            Customer customerAfter = customerDao.findById(orderBefore.getCustomerId());

            System.out.println("付款后：");
            System.out.println("  订单状态 = " + orderAfter.getOrderStatus() +
                    "，支付时间 = " + orderAfter.getPaymentTime());
            System.out.println("  客户余额 = " + customerAfter.getAccountBalance());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



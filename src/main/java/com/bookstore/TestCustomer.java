package com.bookstore;

import com.bookstore.dao.CustomerDao;
import com.bookstore.model.Customer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * 简单测试类：
 * 1）查询并打印当前所有客户；
 * 2）插入一位新客户，再次查询并打印。
 */
public class TestCustomer {

    public static void main(String[] args) {
        CustomerDao dao = new CustomerDao();

        try {
            System.out.println("当前 customer 表中的客户：");
            List<Customer> list = dao.findAll();
            for (Customer c : list) {
                System.out.println(c);
            }

            System.out.println("插入一位新测试客户 wangwu...");
            Customer newCustomer = new Customer();
            newCustomer.setUsername("wangwu");
            newCustomer.setPasswordHash("test-hash-wangwu"); // 课程阶段可先不做真正加密
            newCustomer.setRealName("王五");
            newCustomer.setMobilePhone("13800000003");
            newCustomer.setEmail("wangwu@example.com");
            newCustomer.setAccountBalance(new BigDecimal("300.00"));
            newCustomer.setTotalConsumption(BigDecimal.ZERO);
            newCustomer.setCreditLevelId(2); // 假设二级信用

            int rows = dao.insert(newCustomer);
            System.out.println("插入结果，受影响行数 = " + rows + "，生成的 customer_id = " + newCustomer.getCustomerId());

            System.out.println("再次查询所有客户：");
            list = dao.findAll();
            for (Customer c : list) {
                System.out.println(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



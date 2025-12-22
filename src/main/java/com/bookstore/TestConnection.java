package com.bookstore;

import com.bookstore.dao.BookDao;
import com.bookstore.model.Book;
import com.bookstore.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 简单测试类：
 * 1）验证能否连接数据库；
 * 2）调用 BookDao 查询所有图书并打印。
 */
public class TestConnection {

    public static void main(String[] args) {
        // 1. 测试连接池是否可用
        try (Connection ignored = DBUtil.getConnection()) {
            System.out.println("数据库连接成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // 2. 使用 BookDao 查询所有图书
        BookDao bookDao = new BookDao();
        try {
            List<Book> books = bookDao.findAll();
            System.out.println("当前 book 表中的图书：");
            for (Book b : books) {
                System.out.println(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}




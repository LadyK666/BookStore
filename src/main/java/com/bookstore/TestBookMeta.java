package com.bookstore;

import com.bookstore.dao.AuthorDao;
import com.bookstore.dao.BookAuthorKeywordDao;
import com.bookstore.dao.KeywordDao;
import com.bookstore.model.Author;
import com.bookstore.model.Keyword;

import java.sql.SQLException;
import java.util.List;

/**
 * 测试书目的作者与关键字关联：
 *  1）为 B001/B002 插入作者与关键字并建立关联；
 *  2）查询并打印某本书的作者列表与关键字列表。
 */
public class TestBookMeta {

    public static void main(String[] args) {
        AuthorDao authorDao = new AuthorDao();
        KeywordDao keywordDao = new KeywordDao();
        BookAuthorKeywordDao linkDao = new BookAuthorKeywordDao();

        try {
            // 1. 插入一些作者与关键字（如果已经存在会再次插入新记录，本测试主要用于演示）
            Author a1 = new Author();
            a1.setAuthorName("王珊");
            a1.setNationality("中国");
            a1.setBiography("数据库系统概论教材作者之一。");
            authorDao.insert(a1);

            Author a2 = new Author();
            a2.setAuthorName("Bruce Eckel");
            a2.setNationality("美国");
            a2.setBiography("《Thinking in Java》作者。");
            authorDao.insert(a2);

            Keyword k1 = new Keyword();
            k1.setKeywordText("数据库");
            keywordDao.insert(k1);

            Keyword k2 = new Keyword();
            k2.setKeywordText("Java");
            keywordDao.insert(k2);

            // 2. 建立书目-作者/关键字关系
            // B001: 数据库系统概论 -> 作者 王珊, 关键字 数据库
            linkDao.addBookAuthor("B001", a1.getAuthorId(), 1);
            linkDao.addBookKeyword("B001", k1.getKeywordId());

            // B002: Java 编程思想 -> 作者 Bruce Eckel, 关键字 Java
            linkDao.addBookAuthor("B002", a2.getAuthorId(), 1);
            linkDao.addBookKeyword("B002", k2.getKeywordId());

            // 3. 查询并打印 B001 的作者与关键字
            System.out.println("B001 的作者列表：");
            List<Author> b1Authors = authorDao.findByBookId("B001");
            for (Author a : b1Authors) {
                System.out.println("  " + a);
            }

            System.out.println("B001 的关键字列表：");
            List<Keyword> b1Keywords = keywordDao.findByBookId("B001");
            for (Keyword k : b1Keywords) {
                System.out.println("  " + k);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



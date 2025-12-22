package com.bookstore.web.controller;

import com.bookstore.dao.AuthorDao;
import com.bookstore.dao.BookDao;
import com.bookstore.dao.KeywordDao;
import com.bookstore.model.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

/**
 * 顾客端-书目浏览与查询接口。
 * 逻辑严格复刻 CustomerView.loadAllBooks / searchBooks：
 * - /api/customer/books         -> 全部在售书目
 * - /api/customer/books/search  -> 单字段关键字搜索（书号/书名/出版社/作者/关键字）
 */
@RestController
@RequestMapping("/api/customer/books")
@CrossOrigin
public class CustomerBookController {

    private final BookDao bookDao = new BookDao();
    private final AuthorDao authorDao = new AuthorDao();
    private final KeywordDao keywordDao = new KeywordDao();

    /**
     * 获取全部书目列表。
     */
    @GetMapping
    public ResponseEntity<List<Book>> listAll() throws SQLException {
        List<Book> books = bookDao.findAll();
        return ResponseEntity.ok(books);
    }

    /**
     * 关键字搜索：等价于 CustomerView.searchBooks 的三步搜索与去重逻辑。
     *
     * @param keyword 书号 / 书名 / 出版社 / 作者 / 关键字 任意其一的模糊查询关键字
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> search(@RequestParam("keyword") String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(bookDao.findAll());
        }
        String kw = keyword.trim();
        String kwLower = kw.toLowerCase();

        // 使用 LinkedHashMap 去重并保留顺序
        Map<String, Book> map = new LinkedHashMap<>();

        // 1. 在所有书目的 书号 / 书名 / 出版社 / ISBN 上做不区分大小写匹配
        for (Book b : bookDao.findAll()) {
            String bookId = b.getBookId() == null ? "" : b.getBookId().toLowerCase();
            String title = b.getTitle() == null ? "" : b.getTitle().toLowerCase();
            String publisher = b.getPublisher() == null ? "" : b.getPublisher().toLowerCase();
            String isbn = b.getIsbn() == null ? "" : b.getIsbn().toLowerCase();
            if (bookId.contains(kwLower) || title.contains(kwLower)
                    || publisher.contains(kwLower) || isbn.contains(kwLower)) {
                map.put(b.getBookId(), b);
            }
        }

        // 2. 按作者名模糊匹配
        Set<String> byAuthor = authorDao.findBookIdsByAuthorNameLike(kw);
        for (String bookId : byAuthor) {
            if (!map.containsKey(bookId)) {
                Book b = bookDao.findById(bookId);
                if (b != null) {
                    map.put(bookId, b);
                }
            }
        }

        // 3. 按关键字文本模糊匹配
        Set<String> byKeyword = keywordDao.findBookIdsByKeywordTextLike(kw);
        for (String bookId : byKeyword) {
            if (!map.containsKey(bookId)) {
                Book b = bookDao.findById(bookId);
                if (b != null) {
                    map.put(bookId, b);
                }
            }
        }

        return ResponseEntity.ok(new ArrayList<>(map.values()));
    }
}



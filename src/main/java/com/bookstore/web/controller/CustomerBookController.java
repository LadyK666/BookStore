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
 * - /api/customer/books -> 全部在售书目
 * - /api/customer/books/search -> 单字段关键字搜索（书号/书名/出版社/作者/关键字）
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
     * 获取图书总数。
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getCount() throws SQLException {
        return ResponseEntity.ok(bookDao.countAll());
    }

    /**
     * 获取单本书籍详情，包含作者和关键词。
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookDetail(@PathVariable("bookId") String bookId) throws SQLException {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        var authors = authorDao.findByBookId(bookId);
        var keywords = keywordDao.findByBookId(bookId);

        // 如果是丛书，获取子书目列表
        List<Book> childBooks = null;
        if (book.isSeriesFlag()) {
            childBooks = bookDao.findChildBooks(bookId);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("book", book);
        resp.put("authors", authors);
        resp.put("keywords", keywords);
        if (childBooks != null) {
            resp.put("childBooks", childBooks);
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * 获取所有丛书列表。
     */
    @GetMapping("/series")
    public ResponseEntity<List<Book>> listSeriesBooks() throws SQLException {
        List<Book> series = bookDao.findSeriesBooks();
        return ResponseEntity.ok(series);
    }

    /**
     * 获取丛书的子书目列表。
     */
    @GetMapping("/{bookId}/children")
    public ResponseEntity<List<Book>> listChildBooks(@PathVariable("bookId") String bookId) throws SQLException {
        List<Book> children = bookDao.findChildBooks(bookId);
        return ResponseEntity.ok(children);
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

    /**
     * 高级搜索：按作者查询，可指定作者顺序（第一作者、第二作者等）。
     * 
     * @param author      作者名关键字
     * @param authorOrder 作者顺序（1=第一作者, 2=第二作者, 0=不限）
     */
    @GetMapping("/search/by-author")
    public ResponseEntity<List<Book>> searchByAuthor(
            @RequestParam("author") String author,
            @RequestParam(value = "authorOrder", required = false, defaultValue = "0") Integer authorOrder)
            throws SQLException {
        if (author == null || author.trim().isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        Set<String> bookIds = authorDao.findBookIdsByAuthorNameLikeWithOrder(author, authorOrder);
        List<Book> result = new ArrayList<>();
        for (String bookId : bookIds) {
            Book b = bookDao.findById(bookId);
            if (b != null) {
                result.add(b);
            }
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 高级搜索：按多个关键字查询，可指定最低匹配数。
     * 
     * @param keywords 逗号分隔的关键字列表，如 "数据库,SQL,编程"
     * @param minMatch 最低匹配数（默认1，即匹配任意一个关键字即可）
     */
    @GetMapping("/search/by-keywords")
    public ResponseEntity<List<Map<String, Object>>> searchByKeywords(
            @RequestParam("keywords") String keywords,
            @RequestParam(value = "minMatch", required = false, defaultValue = "1") Integer minMatch)
            throws SQLException {
        if (keywords == null || keywords.trim().isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        String[] kwArray = keywords.split(",");
        List<String> kwList = new ArrayList<>();
        for (String kw : kwArray) {
            if (kw.trim().length() > 0) {
                kwList.add(kw.trim());
            }
        }
        if (kwList.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        Map<String, Integer> matchMap = keywordDao.findBookIdsByKeywordsWithMinMatch(kwList, minMatch);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : matchMap.entrySet()) {
            Book b = bookDao.findById(entry.getKey());
            if (b != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("book", b);
                item.put("matchCount", entry.getValue());
                item.put("totalKeywords", kwList.size());
                result.add(item);
            }
        }
        return ResponseEntity.ok(result);
    }
}

package com.bookstore.web.controller;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * 管理员端 - 书目管理相关接口。
 *
 * 覆盖 AdminView.showBookManagement 及相关子对话框的主要能力：
 * - 查询全部书目；
 * - 新增书目并初始化库存；
 * - 编辑书目详情（基本字段）；
 * - 维护作者 / 关键字 / 供货关系（添加、删除及部分编辑）。
 */
@RestController
@RequestMapping("/api/admin/books")
@CrossOrigin
public class AdminBookController {

    private final BookDao bookDao = new BookDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final AuthorDao authorDao = new AuthorDao();
    private final KeywordDao keywordDao = new KeywordDao();
    private final BookAuthorKeywordDao bookAuthorKeywordDao = new BookAuthorKeywordDao();
    private final SupplyDao supplyDao = new SupplyDao();

    @GetMapping
    public ResponseEntity<List<Book>> listBooks() throws SQLException {
        return ResponseEntity.ok(bookDao.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody AddBookReq req) {
        try {
            if (req == null || req.getBookId() == null || req.getBookId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("书号不能为空"));
            }
            if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("书名不能为空"));
            }
            if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("价格必须为非负数"));
            }
            int initQty = req.getInitQuantity() != null ? req.getInitQuantity() : 0;
            int safety = req.getSafetyStock() != null ? req.getSafetyStock() : 0;
            if (initQty < 0 || safety < 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("初始库存和安全库存必须为非负整数"));
            }

            Book book = new Book();
            book.setBookId(req.getBookId().trim());
            book.setIsbn(req.getIsbn() != null ? req.getIsbn().trim() : null);
            book.setTitle(req.getTitle().trim());
            book.setPublisher(req.getPublisher() != null ? req.getPublisher().trim() : null);
            book.setPrice(req.getPrice());
            book.setStatus("AVAILABLE");
            book.setCoverImageUrl(req.getCoverImageUrl());
            book.setCatalog(req.getCatalog());

            bookDao.insert(book);

            Inventory inv = new Inventory();
            inv.setBookId(book.getBookId());
            inv.setQuantity(initQty);
            inv.setSafetyStock(safety);
            inventoryDao.insert(inv);

            return ResponseEntity.ok(book);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 根据书号查询书目详情（包含作者 / 关键字 / 供货关系）。
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookDetail(@PathVariable String bookId) {
        try {
            Book book = bookDao.findById(bookId);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            BookDetailResp resp = new BookDetailResp();
            resp.setBook(book);
            resp.setAuthors(authorDao.findByBookId(bookId));
            resp.setKeywords(keywordDao.findByBookId(bookId));
            resp.setSupplies(supplyDao.findByBookId(bookId));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 更新书目的基础信息（不涉及库存）。
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<?> updateBook(@PathVariable String bookId, @RequestBody UpdateBookReq req) {
        try {
            Book book = bookDao.findById(bookId);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("书名不能为空"));
            }
            if (req.getPrice() != null && req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("价格必须为非负数"));
            }
            book.setIsbn(req.getIsbn());
            book.setTitle(req.getTitle().trim());
            book.setPublisher(req.getPublisher());
            book.setEdition(req.getEdition());
            book.setPrice(req.getPrice());
            book.setStatus(req.getStatus() != null ? req.getStatus() : book.getStatus());
            book.setCoverImageUrl(req.getCoverImageUrl());
            book.setCatalog(req.getCatalog());
            bookDao.update(book);
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    // ------------ 作者维护 ------------

    @GetMapping("/{bookId}/authors")
    public ResponseEntity<?> listAuthors(@PathVariable String bookId) {
        try {
            return ResponseEntity.ok(authorDao.findByBookId(bookId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PostMapping("/{bookId}/authors")
    public ResponseEntity<?> addAuthorToBook(@PathVariable String bookId, @RequestBody AddAuthorReq req) {
        try {
            if (req == null || (req.getAuthorId() == null && (req.getAuthorName() == null || req.getAuthorName().trim().isEmpty()))) {
                return ResponseEntity.badRequest().body(new ErrorResp("作者姓名不能为空"));
            }
            Long authorId = req.getAuthorId();
            if (authorId == null) {
                Author a = new Author();
                a.setAuthorName(req.getAuthorName().trim());
                a.setNationality(req.getNationality());
                a.setBiography(req.getBiography());
                authorId = authorDao.insert(a);
            }
            int order = req.getAuthorOrder() != null ? req.getAuthorOrder() : 1;
            bookAuthorKeywordDao.addBookAuthor(bookId, authorId, order);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PutMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<?> updateAuthorForBook(@PathVariable String bookId,
                                                 @PathVariable long authorId,
                                                 @RequestBody UpdateAuthorReq req) {
        try {
            if (req.getAuthorName() != null || req.getNationality() != null || req.getBiography() != null) {
                Author a = new Author();
                a.setAuthorId(authorId);
                a.setAuthorName(req.getAuthorName());
                a.setNationality(req.getNationality());
                a.setBiography(req.getBiography());
                authorDao.update(a);
            }
            if (req.getAuthorOrder() != null) {
                bookAuthorKeywordDao.updateBookAuthorOrder(bookId, authorId, req.getAuthorOrder());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @DeleteMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<?> removeAuthorFromBook(@PathVariable String bookId, @PathVariable long authorId) {
        try {
            bookAuthorKeywordDao.removeBookAuthor(bookId, authorId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    // ------------ 关键字维护 ------------

    @GetMapping("/{bookId}/keywords")
    public ResponseEntity<?> listKeywords(@PathVariable String bookId) {
        try {
            return ResponseEntity.ok(keywordDao.findByBookId(bookId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PostMapping("/{bookId}/keywords")
    public ResponseEntity<?> addKeywordToBook(@PathVariable String bookId, @RequestBody AddKeywordReq req) {
        try {
            if (req == null || (req.getKeywordId() == null && (req.getKeywordText() == null || req.getKeywordText().trim().isEmpty()))) {
                return ResponseEntity.badRequest().body(new ErrorResp("关键字不能为空"));
            }
            Long keywordId = req.getKeywordId();
            if (keywordId == null) {
                Keyword k = new Keyword();
                k.setKeywordText(req.getKeywordText().trim());
                keywordId = keywordDao.insert(k);
            }
            bookAuthorKeywordDao.addBookKeyword(bookId, keywordId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PutMapping("/{bookId}/keywords/{keywordId}")
    public ResponseEntity<?> updateKeyword(@PathVariable String bookId,
                                           @PathVariable long keywordId,
                                           @RequestBody UpdateKeywordReq req) {
        try {
            if (req.getKeywordText() != null) {
                Keyword k = new Keyword();
                k.setKeywordId(keywordId);
                k.setKeywordText(req.getKeywordText());
                keywordDao.update(k);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @DeleteMapping("/{bookId}/keywords/{keywordId}")
    public ResponseEntity<?> removeKeywordFromBook(@PathVariable String bookId, @PathVariable long keywordId) {
        try {
            bookAuthorKeywordDao.removeBookKeyword(bookId, keywordId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    // ------------ 供货关系维护 ------------

    @GetMapping("/{bookId}/supplies")
    public ResponseEntity<?> listSupplies(@PathVariable String bookId) {
        try {
            return ResponseEntity.ok(supplyDao.findByBookId(bookId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PostMapping("/{bookId}/supplies")
    public ResponseEntity<?> addSupply(@PathVariable String bookId, @RequestBody AddSupplyReq req) {
        try {
            if (req.getSupplierId() == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("供应商ID不能为空"));
            }
            Supply s = new Supply();
            s.setSupplierId(req.getSupplierId());
            s.setBookId(bookId);
            s.setSupplyPrice(req.getSupplyPrice());
            s.setLeadTimeDays(req.getLeadTimeDays());
            s.setPrimary(Boolean.TRUE.equals(req.getPrimary()));
            supplyDao.insert(s);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PutMapping("/{bookId}/supplies/{supplierId}")
    public ResponseEntity<?> updateSupply(@PathVariable String bookId,
                                          @PathVariable long supplierId,
                                          @RequestBody UpdateSupplyReq req) {
        try {
            Supply s = new Supply();
            s.setSupplierId(supplierId);
            s.setBookId(bookId);
            s.setSupplyPrice(req.getSupplyPrice());
            s.setLeadTimeDays(req.getLeadTimeDays());
            s.setPrimary(Boolean.TRUE.equals(req.getPrimary()));
            supplyDao.update(s);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @DeleteMapping("/{bookId}/supplies/{supplierId}")
    public ResponseEntity<?> deleteSupply(@PathVariable String bookId, @PathVariable long supplierId) {
        try {
            supplyDao.delete(supplierId, bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    public static class AddBookReq {
        private String bookId;
        private String isbn;
        private String title;
        private String publisher;
        private BigDecimal price;
        private String coverImageUrl;
        private String catalog;
        private Integer initQuantity;
        private Integer safetyStock;

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getCoverImageUrl() {
            return coverImageUrl;
        }

        public void setCoverImageUrl(String coverImageUrl) {
            this.coverImageUrl = coverImageUrl;
        }

        public String getCatalog() {
            return catalog;
        }

        public void setCatalog(String catalog) {
            this.catalog = catalog;
        }

        public Integer getInitQuantity() {
            return initQuantity;
        }

        public void setInitQuantity(Integer initQuantity) {
            this.initQuantity = initQuantity;
        }

        public Integer getSafetyStock() {
            return safetyStock;
        }

        public void setSafetyStock(Integer safetyStock) {
            this.safetyStock = safetyStock;
        }
    }

    public static class UpdateBookReq {
        private String isbn;
        private String title;
        private String publisher;
        private String edition;
        private BigDecimal price;
        private String status;
        private String coverImageUrl;
        private String catalog;

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getEdition() {
            return edition;
        }

        public void setEdition(String edition) {
            this.edition = edition;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCoverImageUrl() {
            return coverImageUrl;
        }

        public void setCoverImageUrl(String coverImageUrl) {
            this.coverImageUrl = coverImageUrl;
        }

        public String getCatalog() {
            return catalog;
        }

        public void setCatalog(String catalog) {
            this.catalog = catalog;
        }
    }

    public static class AddAuthorReq {
        private Long authorId;
        private String authorName;
        private String nationality;
        private String biography;
        private Integer authorOrder;

        public Long getAuthorId() {
            return authorId;
        }

        public void setAuthorId(Long authorId) {
            this.authorId = authorId;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getBiography() {
            return biography;
        }

        public void setBiography(String biography) {
            this.biography = biography;
        }

        public Integer getAuthorOrder() {
            return authorOrder;
        }

        public void setAuthorOrder(Integer authorOrder) {
            this.authorOrder = authorOrder;
        }
    }

    public static class UpdateAuthorReq {
        private String authorName;
        private String nationality;
        private String biography;
        private Integer authorOrder;

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getBiography() {
            return biography;
        }

        public void setBiography(String biography) {
            this.biography = biography;
        }

        public Integer getAuthorOrder() {
            return authorOrder;
        }

        public void setAuthorOrder(Integer authorOrder) {
            this.authorOrder = authorOrder;
        }
    }

    public static class AddKeywordReq {
        private Long keywordId;
        private String keywordText;

        public Long getKeywordId() {
            return keywordId;
        }

        public void setKeywordId(Long keywordId) {
            this.keywordId = keywordId;
        }

        public String getKeywordText() {
            return keywordText;
        }

        public void setKeywordText(String keywordText) {
            this.keywordText = keywordText;
        }
    }

    public static class UpdateKeywordReq {
        private String keywordText;

        public String getKeywordText() {
            return keywordText;
        }

        public void setKeywordText(String keywordText) {
            this.keywordText = keywordText;
        }
    }

    public static class AddSupplyReq {
        private Long supplierId;
        private java.math.BigDecimal supplyPrice;
        private Integer leadTimeDays;
        private Boolean primary;

        public Long getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(Long supplierId) {
            this.supplierId = supplierId;
        }

        public java.math.BigDecimal getSupplyPrice() {
            return supplyPrice;
        }

        public void setSupplyPrice(java.math.BigDecimal supplyPrice) {
            this.supplyPrice = supplyPrice;
        }

        public Integer getLeadTimeDays() {
            return leadTimeDays;
        }

        public void setLeadTimeDays(Integer leadTimeDays) {
            this.leadTimeDays = leadTimeDays;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }
    }

    public static class UpdateSupplyReq {
        private java.math.BigDecimal supplyPrice;
        private Integer leadTimeDays;
        private Boolean primary;

        public java.math.BigDecimal getSupplyPrice() {
            return supplyPrice;
        }

        public void setSupplyPrice(java.math.BigDecimal supplyPrice) {
            this.supplyPrice = supplyPrice;
        }

        public Integer getLeadTimeDays() {
            return leadTimeDays;
        }

        public void setLeadTimeDays(Integer leadTimeDays) {
            this.leadTimeDays = leadTimeDays;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }
    }

    public static class BookDetailResp {
        private Book book;
        private java.util.List<Author> authors;
        private java.util.List<Keyword> keywords;
        private java.util.List<Supply> supplies;

        public Book getBook() {
            return book;
        }

        public void setBook(Book book) {
            this.book = book;
        }

        public java.util.List<Author> getAuthors() {
            return authors;
        }

        public void setAuthors(java.util.List<Author> authors) {
            this.authors = authors;
        }

        public java.util.List<Keyword> getKeywords() {
            return keywords;
        }

        public void setKeywords(java.util.List<Keyword> keywords) {
            this.keywords = keywords;
        }

        public java.util.List<Supply> getSupplies() {
            return supplies;
        }

        public void setSupplies(java.util.List<Supply> supplies) {
            this.supplies = supplies;
        }
    }

    public static class ErrorResp {
        private String message;

        public ErrorResp(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}



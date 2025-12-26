package com.bookstore.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 书目实体类，对应表：book。
 */
public class Book {

    private String bookId;
    private String isbn;
    private String title;
    private String publisher;
    private LocalDate publishDate;
    private String edition;
    private BigDecimal price;
    private String status;
    private String coverImageUrl;
    private String catalog;
    // 丛书支持字段
    private boolean seriesFlag; // 是否为丛书
    private String parentBookId; // 父书号（如果是丛书的子书）

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

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
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

    public boolean isSeriesFlag() {
        return seriesFlag;
    }

    public void setSeriesFlag(boolean seriesFlag) {
        this.seriesFlag = seriesFlag;
    }

    public String getParentBookId() {
        return parentBookId;
    }

    public void setParentBookId(String parentBookId) {
        this.parentBookId = parentBookId;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publishDate=" + publishDate +
                ", edition='" + edition + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", seriesFlag=" + seriesFlag +
                ", parentBookId='" + parentBookId + '\'' +
                '}';
    }
}

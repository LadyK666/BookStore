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
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", catalog='" + (catalog != null ? catalog.substring(0, Math.min(20, catalog.length())) + "..." : null) + '\'' +
                '}';
    }
}



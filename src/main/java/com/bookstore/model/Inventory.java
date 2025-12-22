package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * 库存实体类，对应表 inventory
 */
public class Inventory {
    private String bookId;
    private int quantity;
    /**
     * 安全库存，对应字段 safety_stock
     */
    private int safetyStock;
    /**
     * 库位编码，对应字段 location_code
     */
    private String locationCode;
    private LocalDateTime updateTime;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(int safetyStock) {
        this.safetyStock = safetyStock;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "bookId='" + bookId + '\'' +
                ", quantity=" + quantity +
                ", safetyStock=" + safetyStock +
                ", locationCode='" + locationCode + '\'' +
                '}';
    }
}


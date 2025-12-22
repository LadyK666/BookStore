package com.bookstore.model;

import java.math.BigDecimal;

/**
 * 供货关系实体，对应表：supply。
 */
public class Supply {

    private Long supplierId;
    private String bookId;
    private BigDecimal supplyPrice;
    private Integer leadTimeDays;
    private boolean primary;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public BigDecimal getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(BigDecimal supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return "Supply{" +
                "supplierId=" + supplierId +
                ", bookId='" + bookId + '\'' +
                ", supplyPrice=" + supplyPrice +
                ", leadTimeDays=" + leadTimeDays +
                ", primary=" + primary +
                '}';
    }
}



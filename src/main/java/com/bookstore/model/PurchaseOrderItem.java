package com.bookstore.model;

import java.math.BigDecimal;

/**
 * 采购明细实体
 */
public class PurchaseOrderItem {
    private Long purchaseOrderId;
    private String bookId;
    private Integer purchaseQuantity;
    private BigDecimal purchasePrice;
    private Long relatedOutOfStockId;  // 可选，关联缺书记录

    public PurchaseOrderItem() {}

    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public Integer getPurchaseQuantity() { return purchaseQuantity; }
    public void setPurchaseQuantity(Integer purchaseQuantity) { this.purchaseQuantity = purchaseQuantity; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public Long getRelatedOutOfStockId() { return relatedOutOfStockId; }
    public void setRelatedOutOfStockId(Long relatedOutOfStockId) { this.relatedOutOfStockId = relatedOutOfStockId; }

    @Override
    public String toString() {
        return "PurchaseOrderItem{" +
                "purchaseOrderId=" + purchaseOrderId +
                ", bookId='" + bookId + '\'' +
                ", purchaseQuantity=" + purchaseQuantity +
                ", purchasePrice=" + purchasePrice +
                ", relatedOutOfStockId=" + relatedOutOfStockId +
                '}';
    }
}


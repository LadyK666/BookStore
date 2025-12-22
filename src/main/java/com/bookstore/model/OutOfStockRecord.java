package com.bookstore.model;

import java.time.LocalDate;

/**
 * 缺书记录实体
 */
public class OutOfStockRecord {
    private Long recordId;
    private String bookId;
    private Integer requiredQuantity;
    private LocalDate recordDate;
    private String source;          // MANUAL, LOW_STOCK, ORDER_EXCEED, CUSTOMER_REQUEST
    private Long relatedCustomerId; // 可选，关联客户
    private String status;          // PENDING, PURCHASING, COMPLETED
    private Integer priority;

    public OutOfStockRecord() {}

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public Integer getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Integer requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getRelatedCustomerId() { return relatedCustomerId; }
    public void setRelatedCustomerId(Long relatedCustomerId) { this.relatedCustomerId = relatedCustomerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "OutOfStockRecord{" +
                "recordId=" + recordId +
                ", bookId='" + bookId + '\'' +
                ", requiredQuantity=" + requiredQuantity +
                ", recordDate=" + recordDate +
                ", source='" + source + '\'' +
                ", relatedCustomerId=" + relatedCustomerId +
                ", status='" + status + '\'' +
                ", priority=" + priority +
                '}';
    }
}


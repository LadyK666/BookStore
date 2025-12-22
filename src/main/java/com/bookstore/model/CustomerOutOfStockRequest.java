package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * 顾客缺书登记实体，对应表：customer_out_of_stock_request。
 * 与正式的缺书记录 out_of_stock_record 区分开，用于记录顾客层面的缺书意向。
 */
public class CustomerOutOfStockRequest {

    private Long requestId;
    private Long orderId;
    private Long customerId;
    private String bookId;
    private Integer requestedQty;
    private String customerNote;
    private boolean paid;
    private String processedStatus; // PENDING / ACCEPTED / REJECTED
    private Long relatedRecordId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private boolean customerNotified;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getRequestedQty() {
        return requestedQty;
    }

    public void setRequestedQty(Integer requestedQty) {
        this.requestedQty = requestedQty;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getProcessedStatus() {
        return processedStatus;
    }

    public void setProcessedStatus(String processedStatus) {
        this.processedStatus = processedStatus;
    }

    public Long getRelatedRecordId() {
        return relatedRecordId;
    }

    public void setRelatedRecordId(Long relatedRecordId) {
        this.relatedRecordId = relatedRecordId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public boolean isCustomerNotified() {
        return customerNotified;
    }

    public void setCustomerNotified(boolean customerNotified) {
        this.customerNotified = customerNotified;
    }
}



package com.bookstore.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购单实体
 */
public class PurchaseOrder {
    private Long purchaseOrderId;
    private Long supplierId;
    private LocalDate createDate;
    private LocalDate expectedDate;
    private String buyer;
    private BigDecimal estimatedAmount;
    private String status;  // DRAFT, ISSUED, PARTIAL_RECEIVED, COMPLETED, CANCELLED

    public PurchaseOrder() {}

    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public LocalDate getCreateDate() { return createDate; }
    public void setCreateDate(LocalDate createDate) { this.createDate = createDate; }

    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }

    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }

    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "purchaseOrderId=" + purchaseOrderId +
                ", supplierId=" + supplierId +
                ", createDate=" + createDate +
                ", expectedDate=" + expectedDate +
                ", buyer='" + buyer + '\'' +
                ", estimatedAmount=" + estimatedAmount +
                ", status='" + status + '\'' +
                '}';
    }
}


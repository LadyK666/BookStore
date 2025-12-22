package com.bookstore.model;

import java.math.BigDecimal;

/**
 * 订单明细实体，对应表：sales_order_item。
 */
public class SalesOrderItem {

    private Long orderItemId;
    private Long orderId;
    private String bookId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subAmount;
    private String itemStatus;
    // 已发货数量
    private Integer shippedQuantity;
    // 已收货数量
    private Integer receivedQuantity;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubAmount() {
        return subAmount;
    }

    public void setSubAmount(BigDecimal subAmount) {
        this.subAmount = subAmount;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public Integer getShippedQuantity() {
        return shippedQuantity;
    }

    public void setShippedQuantity(Integer shippedQuantity) {
        this.shippedQuantity = shippedQuantity;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    @Override
    public String toString() {
        return "SalesOrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", bookId='" + bookId + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subAmount=" + subAmount +
                ", itemStatus='" + itemStatus + '\'' +
                ", shippedQuantity=" + shippedQuantity +
                ", receivedQuantity=" + receivedQuantity +
                '}';
    }
}



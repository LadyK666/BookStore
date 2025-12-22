package com.bookstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主实体，对应表：sales_order。
 * 本阶段只用到部分核心字段。
 */
public class SalesOrder {

    private Long orderId;
    private Long customerId;
    private LocalDateTime orderTime;
    private String orderStatus;
    private BigDecimal goodsAmount;
    private BigDecimal discountRateSnapshot;
    private BigDecimal payableAmount;
    private String shippingAddressSnapshot;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private String customerNote;

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

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getGoodsAmount() {
        return goodsAmount;
    }

    public void setGoodsAmount(BigDecimal goodsAmount) {
        this.goodsAmount = goodsAmount;
    }

    public BigDecimal getDiscountRateSnapshot() {
        return discountRateSnapshot;
    }

    public void setDiscountRateSnapshot(BigDecimal discountRateSnapshot) {
        this.discountRateSnapshot = discountRateSnapshot;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public String getShippingAddressSnapshot() {
        return shippingAddressSnapshot;
    }

    public void setShippingAddressSnapshot(String shippingAddressSnapshot) {
        this.shippingAddressSnapshot = shippingAddressSnapshot;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    @Override
    public String toString() {
        return "SalesOrder{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", orderTime=" + orderTime +
                ", orderStatus='" + orderStatus + '\'' +
                ", goodsAmount=" + goodsAmount +
                ", discountRateSnapshot=" + discountRateSnapshot +
                ", payableAmount=" + payableAmount +
                ", shippingAddressSnapshot='" + shippingAddressSnapshot + '\'' +
                '}';
    }
}



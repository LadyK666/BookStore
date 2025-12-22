package com.bookstore.model;

/**
 * 发货明细实体，对应表：shipment_item。
 */
public class ShipmentItem {

    private Long shipmentItemId;
    private Long shipmentId;
    private Long orderItemId;
    private Integer shipQuantity;
    private String receiveStatus; // PENDING / RECEIVED
    private Integer receivedQuantity;
    private java.time.LocalDateTime receivedTime;

    public Long getShipmentItemId() {
        return shipmentItemId;
    }

    public void setShipmentItemId(Long shipmentItemId) {
        this.shipmentItemId = shipmentItemId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getShipQuantity() {
        return shipQuantity;
    }

    public void setShipQuantity(Integer shipQuantity) {
        this.shipQuantity = shipQuantity;
    }

    public String getReceiveStatus() {
        return receiveStatus;
    }

    public void setReceiveStatus(String receiveStatus) {
        this.receiveStatus = receiveStatus;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public java.time.LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(java.time.LocalDateTime receivedTime) {
        this.receivedTime = receivedTime;
    }

    @Override
    public String toString() {
        return "ShipmentItem{" +
                "shipmentItemId=" + shipmentItemId +
                ", shipmentId=" + shipmentId +
                ", orderItemId=" + orderItemId +
                ", shipQuantity=" + shipQuantity +
                ", receiveStatus='" + receiveStatus + '\'' +
                ", receivedQuantity=" + receivedQuantity +
                ", receivedTime=" + receivedTime +
                '}';
    }
}



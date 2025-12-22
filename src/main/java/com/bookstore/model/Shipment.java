package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * 发货单主实体，对应表：shipment。
 */
public class Shipment {

    private Long shipmentId;
    private Long orderId;
    private LocalDateTime shipTime;
    private String carrier;
    private String trackingNumber;
    private String shipmentStatus;
    private String operator;

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getShipTime() {
        return shipTime;
    }

    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(String shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "shipmentId=" + shipmentId +
                ", orderId=" + orderId +
                ", shipTime=" + shipTime +
                ", carrier='" + carrier + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", shipmentStatus='" + shipmentStatus + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}



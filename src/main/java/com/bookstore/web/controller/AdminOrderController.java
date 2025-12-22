package com.bookstore.web.controller;

import com.bookstore.dao.SalesOrderDao;
import com.bookstore.dao.ShipmentDao;
import com.bookstore.model.SalesOrder;
import com.bookstore.model.SalesOrderItem;
import com.bookstore.model.Shipment;
import com.bookstore.model.ShipmentItem;
import com.bookstore.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理员端 - 订单管理相关接口。
 * 对应 AdminView.showOrderManagement / showOrderDetailForAdmin：
 * - 支持按状态筛选所有订单；
 * - 支持查看任意订单的明细与发货记录。
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminOrderController {

    private final SalesOrderDao salesOrderDao = new SalesOrderDao();
    private final ShipmentDao shipmentDao = new ShipmentDao();
    private final ShipmentService shipmentService = new ShipmentService();

    /**
     * 管理员查看订单列表。
     *
     * @param status 订单状态；传 "全部" 或为空则不过滤。
     */
    @GetMapping("/orders")
    public ResponseEntity<List<SalesOrder>> listOrders(
            @RequestParam(value = "status", required = false) String status) throws SQLException {
        List<SalesOrder> orders;
        if (status == null || status.isEmpty() || "全部".equals(status)) {
            orders = salesOrderDao.findAll();
        } else {
            orders = salesOrderDao.findByStatus(status);
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * 管理员查看单个订单的明细及发货记录。
     * 逻辑与 CustomerOrderController.orderDetail 基本一致，只是面向管理员。
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResp> orderDetail(@PathVariable("orderId") long orderId)
            throws SQLException {
        SalesOrder order = salesOrderDao.findOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
        List<Shipment> shipments = shipmentDao.findByOrderId(orderId);

        OrderDetailResp resp = new OrderDetailResp();
        resp.setOrder(order);
        resp.setItems(items != null ? items : new ArrayList<>());
        resp.setShipments(shipments != null ? shipments : new ArrayList<>());
        return ResponseEntity.ok(resp);
    }

    /**
     * 管理员整单发货（对应 AdminView.showShipmentManagement -> showShipDialog）。
     */
    @PostMapping("/orders/{orderId}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable("orderId") long orderId,
                                       @RequestBody ShipReq req) {
        try {
            shipmentService.shipOrder(orderId, req.getCarrier(), req.getTrackingNumber(), req.getOperator());
            SalesOrder updated = salesOrderDao.findOrderById(orderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 管理员分次发货（对应 AdminView.showShipmentManagement -> showPartialShipDialog）。
     * 仅处理 shipQuantity > 0 的条目。
     */
    @PostMapping("/orders/{orderId}/ship/partial")
    public ResponseEntity<?> shipOrderPartially(@PathVariable("orderId") long orderId,
                                                @RequestBody PartialShipReq req) {
        try {
            List<ShipmentItem> toShip = new ArrayList<>();
            if (req.getItems() != null) {
                for (PartialItem pi : req.getItems()) {
                    if (pi.getOrderItemId() == null || pi.getShipQuantity() == null || pi.getShipQuantity() <= 0) {
                        continue;
                    }
                    ShipmentItem si = new ShipmentItem();
                    si.setOrderItemId(pi.getOrderItemId());
                    si.setShipQuantity(pi.getShipQuantity());
                    toShip.add(si);
                }
            }
            if (toShip.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("没有需要发货的明细数量"));
            }
            shipmentService.shipOrderPartially(orderId, toShip, req.getCarrier(), req.getTrackingNumber(), req.getOperator());
            SalesOrder updated = salesOrderDao.findOrderById(orderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    public static class OrderDetailResp {
        private SalesOrder order;
        private List<SalesOrderItem> items;
        private List<Shipment> shipments;

        public SalesOrder getOrder() {
            return order;
        }

        public void setOrder(SalesOrder order) {
            this.order = order;
        }

        public List<SalesOrderItem> getItems() {
            return items;
        }

        public void setItems(List<SalesOrderItem> items) {
            this.items = items;
        }

        public List<Shipment> getShipments() {
            return shipments;
        }

        public void setShipments(List<Shipment> shipments) {
            this.shipments = shipments;
        }
    }

    public static class ShipReq {
        private String carrier;
        private String trackingNumber;
        private String operator;

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

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }

    public static class PartialShipReq {
        private String carrier;
        private String trackingNumber;
        private String operator;
        private List<PartialItem> items;

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

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public List<PartialItem> getItems() {
            return items;
        }

        public void setItems(List<PartialItem> items) {
            this.items = items;
        }
    }

    public static class PartialItem {
        private Long orderItemId;
        private Integer shipQuantity;

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
    }

    public static class ErrorResp {
        private String message;

        public ErrorResp(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}



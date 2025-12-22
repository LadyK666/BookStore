package com.bookstore.web.controller;

import com.bookstore.dao.CreditLevelDao;
import com.bookstore.dao.CustomerDao;
import com.bookstore.dao.SalesOrderDao;
import com.bookstore.dao.ShipmentDao;
import com.bookstore.dao.CustomerOutOfStockRequestDao;
import com.bookstore.dao.OutOfStockRecordDao;
import com.bookstore.dao.InventoryDao;
import com.bookstore.model.CreditLevel;
import com.bookstore.model.Customer;
import com.bookstore.model.SalesOrder;
import com.bookstore.model.SalesOrderItem;
import com.bookstore.model.Shipment;
import com.bookstore.model.CustomerOutOfStockRequest;
import com.bookstore.model.OutOfStockRecord;
import com.bookstore.service.OrderService;
import com.bookstore.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 顾客端 - 订单相关接口。
 * 逻辑严格参考 CustomerView.showMyOrders / showOrderDetail：
 * - 按顾客 ID + 状态筛选订单
 * - 查询订单明细及发货记录
 */
@RestController
@RequestMapping("/api/customer")
@CrossOrigin
public class CustomerOrderController {

    private final SalesOrderDao salesOrderDao = new SalesOrderDao();
    private final ShipmentDao shipmentDao = new ShipmentDao();
    private final OrderService orderService = new OrderService();
    private final CustomerDao customerDao = new CustomerDao();
    private final CreditLevelDao creditLevelDao = new CreditLevelDao();
    private final ShipmentService shipmentService = new ShipmentService();
    private final CustomerOutOfStockRequestDao customerOutOfStockRequestDao = new CustomerOutOfStockRequestDao();
    private final OutOfStockRecordDao outOfStockRecordDao = new OutOfStockRecordDao();
    private final InventoryDao inventoryDao = new InventoryDao();

    /**
     * 获取顾客的订单列表，支持按状态筛选。
     *
     * @param customerId 顾客 ID
     * @param status     订单状态；传 "全部" 或为空则不过滤
     */
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<List<SalesOrder>> listOrders(@PathVariable("customerId") long customerId,
                                                       @RequestParam(value = "status", required = false) String status)
            throws SQLException {
        List<SalesOrder> orders = salesOrderDao.findByCustomerId(customerId);
        if (status != null && !status.isEmpty() && !"全部".equals(status)) {
            List<SalesOrder> filtered = new ArrayList<>();
            for (SalesOrder o : orders) {
                if (status.equals(o.getOrderStatus())) {
                    filtered.add(o);
                }
            }
            orders = filtered;
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * 单个订单的明细及发货记录。
     * 对应 CustomerView.showOrderDetail 的数据来源。
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResp> orderDetail(@PathVariable("orderId") long orderId) throws SQLException {
        SalesOrder order = salesOrderDao.findOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
        List<Shipment> shipments = shipmentDao.findByOrderId(orderId);

        OrderDetailResp resp = new OrderDetailResp();
        resp.setOrder(order);
        resp.setItems(items);
        resp.setShipments(shipments);
        return ResponseEntity.ok(resp);
    }

    /**
     * 顾客创建订单（基础版，下单 + 金额计算，暂未引入缺书登记交互）。
     * 逻辑基于 CustomerView.submitOrder 的“计算商品金额 + 创建订单”部分：
     * - 根据顾客当前信用等级折扣计算折后价与总金额
     * - 创建订单主表及明细
     * - 订单初始状态为 PENDING_PAYMENT
     */
    @PostMapping("/{customerId}/orders")
    public ResponseEntity<?> createOrder(@PathVariable("customerId") long customerId,
                                         @RequestBody CreateOrderRequest req) {
        try {
            if (req.getItems() == null || req.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("购物车为空"));
            }
            // 加载顾客与信用等级信息
            Customer customer = customerDao.findById(customerId);
            if (customer == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("顾客不存在"));
            }
            CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
            if (level == null || level.getDiscountRate() == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("未找到顾客信用等级配置"));
            }

            BigDecimal discount = level.getDiscountRate();

            // 计算总金额（这里假定前端传来的 unitPrice 为原定价，折扣以当前等级为准）
            List<SalesOrderItem> items = new ArrayList<>();
            BigDecimal goodsAmount = BigDecimal.ZERO;
            for (CreateOrderItem ci : req.getItems()) {
                if (ci.getQuantity() == null || ci.getQuantity() <= 0) continue;
                BigDecimal originPrice = ci.getUnitPrice() != null ? ci.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal unitPrice = originPrice.multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal sub = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));

                SalesOrderItem item = new SalesOrderItem();
                item.setBookId(ci.getBookId());
                item.setQuantity(ci.getQuantity());
                item.setUnitPrice(unitPrice);
                item.setSubAmount(sub);
                item.setItemStatus("ORDERED");
                items.add(item);

                goodsAmount = goodsAmount.add(sub);
            }
            if (items.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("有效的商品项为空"));
            }

            String snapshot = req.getShippingAddressSnapshot();
            if (snapshot == null || snapshot.trim().isEmpty()) {
                snapshot = customer.getRealName() != null ? customer.getRealName() : customer.getUsername();
            }

            SalesOrder order = new SalesOrder();
            order.setCustomerId(customerId);
            order.setOrderTime(LocalDateTime.now());
            order.setOrderStatus("PENDING_PAYMENT");
            order.setGoodsAmount(goodsAmount);
            order.setDiscountRateSnapshot(discount);
            order.setPayableAmount(goodsAmount);
            order.setShippingAddressSnapshot(snapshot);
            order.setCustomerNote(req.getCustomerNote());

            salesOrderDao.createOrder(order, items);

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp("下单失败：" + e.getMessage()));
        }
    }

    /**
     * 顾客对订单进行付款。
     * 逻辑与 CustomerView.showMyOrders() 中点击“付款”按钮时调用 OrderService.payOrder 完全一致。
     */
    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<?> pay(@PathVariable("orderId") long orderId) {
        try {
            orderService.payOrder(orderId);
            SalesOrder updated = salesOrderDao.findOrderById(orderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 顾客确认收货（可分次收货）。
     * 对应 CustomerView.showReceiveDialog 中点击“确认收货”时调用 ShipmentService.confirmReceipt。
     * 前端以 JSON 对象形式提交：{ "orderItemId": 本次确认收货数量, ... }。
     */
    @PostMapping("/orders/{orderId}/receive")
    public ResponseEntity<?> confirmReceive(@PathVariable("orderId") long orderId,
                                            @RequestBody Map<Long, Integer> receiveMap) {
        try {
            shipmentService.confirmReceipt(orderId, receiveMap);
            SalesOrder updated = salesOrderDao.findOrderById(orderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 检查指定订单中哪些明细当前库存不足，用于 Web 端弹出“缺书登记”对话框。
     * 对应 CustomerView.submitOrder() 中基于 InventoryDao 的 shortageItems 计算逻辑。
     */
    @GetMapping("/orders/{orderId}/shortages")
    public ResponseEntity<List<ShortageItemResp>> listShortages(@PathVariable("orderId") long orderId)
            throws SQLException {
        SalesOrder order = salesOrderDao.findOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
        List<ShortageItemResp> shortageList = new ArrayList<>();
        for (SalesOrderItem item : items) {
            int currentQty = inventoryDao.getQuantity(item.getBookId());
            if (currentQty < item.getQuantity()) {
                ShortageItemResp s = new ShortageItemResp();
                s.setOrderItemId(item.getOrderItemId());
                s.setBookId(item.getBookId());
                s.setQuantity(item.getQuantity());
                s.setCurrentStock(currentQty);
                shortageList.add(s);
            }
        }
        return ResponseEntity.ok(shortageList);
    }

    /**
     * Web 端“缺书登记决策”提交接口。
     * 逻辑等价于 CustomerView.createCustomerRequestsAndOutOfStockRecords() +
     * 决策为“仅登记”时将订单状态置为 OUT_OF_STOCK_PENDING。
     *
     * 决策枚举：
     * - PAY_AND_CREATE：已付款并自动生成缺书记录（会立即调用 OrderService.payOrder）。
     * - REQUEST_ONLY：仅登记，暂不付款，订单状态改为 OUT_OF_STOCK_PENDING。
     */
    @PostMapping("/orders/{orderId}/shortages/decision")
    public ResponseEntity<?> handleShortageDecision(@PathVariable("orderId") long orderId,
                                                    @RequestBody ShortageDecisionReq req) {
        try {
            SalesOrder order = salesOrderDao.findOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            List<SalesOrderItem> items = salesOrderDao.findItemsByOrderId(orderId);
            // 重新计算当前缺书明细，保证与最新库存一致
            List<SalesOrderItem> shortageItems = new ArrayList<>();
            for (SalesOrderItem item : items) {
                int currentQty = inventoryDao.getQuantity(item.getBookId());
                if (currentQty < item.getQuantity()) {
                    shortageItems.add(item);
                }
            }
            if (shortageItems.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("当前订单已无缺书项，无需登记"));
            }

            boolean payAndCreate = "PAY_AND_CREATE".equalsIgnoreCase(req.getDecision());
            createCustomerRequestsAndOutOfStockRecords(order, shortageItems, req.getCustomerNote(), payAndCreate);

            if (!payAndCreate) {
                // 方案二：暂不付款，仅提交顾客缺书登记，等待管理员决策
                salesOrderDao.updateStatusAndPaymentTime(orderId, "OUT_OF_STOCK_PENDING", null);
            } else {
                // 方案一：付款并自动生成缺书记录
                orderService.payOrder(orderId);
            }

            SalesOrder updated = salesOrderDao.findOrderById(orderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 根据顾客缺书登记创建正式缺书记录和/或顾客缺书登记记录。
     * 代码基本等价于 CustomerView.createCustomerRequestsAndOutOfStockRecords()，仅从 UI 迁移到 Service/Web 层。
     */
    private void createCustomerRequestsAndOutOfStockRecords(SalesOrder order,
                                                            List<SalesOrderItem> shortageItems,
                                                            String note,
                                                            boolean paidAndAuto) throws SQLException {
        for (SalesOrderItem item : shortageItems) {
            String bookId = item.getBookId();
            int requestedQty = item.getQuantity();

            Long relatedRecordId = null;
            if (paidAndAuto) {
                // 已付款：直接生成/累加正式缺书记录
                OutOfStockRecord record = new OutOfStockRecord();
                record.setBookId(bookId);
                record.setRequiredQuantity(requestedQty);
                record.setRecordDate(java.time.LocalDate.now());
                record.setSource("CUSTOMER_REQUEST");
                record.setRelatedCustomerId(order.getCustomerId());
                record.setStatus("PENDING");
                record.setPriority(1);
                long rid = outOfStockRecordDao.insert(record);
                relatedRecordId = rid;
            }

            // 无论是否已经生成正式缺书记录，都记录一条顾客缺书登记
            CustomerOutOfStockRequest r = new CustomerOutOfStockRequest();
            r.setOrderId(order.getOrderId());
            r.setCustomerId(order.getCustomerId());
            r.setBookId(bookId);
            r.setRequestedQty(requestedQty);
            r.setCustomerNote(note);
            r.setPaid(paidAndAuto);
            r.setProcessedStatus(paidAndAuto ? "ACCEPTED" : "PENDING");
            r.setRelatedRecordId(relatedRecordId);
            if (paidAndAuto) {
                r.setCustomerNotified(true);
                r.setProcessedAt(LocalDateTime.now());
            }
            customerOutOfStockRequestDao.insert(r);
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

    public static class ShortageItemResp {
        private Long orderItemId;
        private String bookId;
        private Integer quantity;
        private Integer currentStock;

        public Long getOrderItemId() {
            return orderItemId;
        }

        public void setOrderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
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

        public Integer getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(Integer currentStock) {
            this.currentStock = currentStock;
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

    public static class ShortageDecisionReq {
        /**
         * 决策类型：PAY_AND_CREATE / REQUEST_ONLY
         */
        private String decision;

        /**
         * 顾客备注，可为空。
         */
        private String customerNote;

        public String getDecision() {
            return decision;
        }

        public void setDecision(String decision) {
            this.decision = decision;
        }

        public String getCustomerNote() {
            return customerNote;
        }

        public void setCustomerNote(String customerNote) {
            this.customerNote = customerNote;
        }
    }

    public static class CreateOrderItem {
        private String bookId;
        private Integer quantity;
        private BigDecimal unitPrice;

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
    }

    public static class CreateOrderRequest {
        private List<CreateOrderItem> items;
        private String shippingAddressSnapshot;
        private String customerNote;

        public List<CreateOrderItem> getItems() {
            return items;
        }

        public void setItems(List<CreateOrderItem> items) {
            this.items = items;
        }

        public String getShippingAddressSnapshot() {
            return shippingAddressSnapshot;
        }

        public void setShippingAddressSnapshot(String shippingAddressSnapshot) {
            this.shippingAddressSnapshot = shippingAddressSnapshot;
        }

        public String getCustomerNote() {
            return customerNote;
        }

        public void setCustomerNote(String customerNote) {
            this.customerNote = customerNote;
        }
    }
}



package com.bookstore.web.controller;

import com.bookstore.dao.CustomerNotificationDao;
import com.bookstore.dao.CustomerOutOfStockRequestDao;
import com.bookstore.dao.OutOfStockRecordDao;
import com.bookstore.dao.PurchaseOrderDao;
import com.bookstore.dao.SalesOrderDao;
import com.bookstore.model.CustomerNotification;
import com.bookstore.model.CustomerOutOfStockRequest;
import com.bookstore.model.OutOfStockRecord;
import com.bookstore.model.PurchaseOrder;
import com.bookstore.model.PurchaseOrderItem;
import com.bookstore.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员端 - 采购管理相关接口。
 *
 * 对应 AdminView.showPurchaseManagement / createOutOfStockTable /
 * createCustomerOutOfStockRequestTable / showCreatePurchaseFromOutOfStockDialog /
 * createPurchaseOrderTable：
 * - 查询待处理缺书记录；
 * - 查询顾客缺书登记并决定是否生成缺书记录；
 * - 根据选中缺书记录生成采购单；
 * - 查询采购单及其明细、到货处理。
 */
@RestController
@RequestMapping("/api/admin/purchase")
@CrossOrigin
public class AdminPurchaseController {

    private final OutOfStockRecordDao outOfStockRecordDao = new OutOfStockRecordDao();
    private final CustomerOutOfStockRequestDao customerReqDao = new CustomerOutOfStockRequestDao();
    private final SalesOrderDao salesOrderDao = new SalesOrderDao();
    private final PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
    private final PurchaseService purchaseService = new PurchaseService();
    private final CustomerNotificationDao notificationDao = new CustomerNotificationDao();

    /**
     * 查询所有待处理(PENDING)的缺书记录。
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<OutOfStockRecord>> listPendingOutOfStock() throws SQLException {
        return ResponseEntity.ok(outOfStockRecordDao.findByStatus("PENDING"));
    }

    /**
     * 手动添加一条缺书记录。
     * 对应 AdminView.showAddOutOfStockDialog：
     * - 仅需填写书号与需求数量；
     * - source 固定为 MANUAL，status 固定为 PENDING，priority 默认 1；
     * - 具体“同一本书 + 同一状态只保留一条记录并累加数量”的规则由 OutOfStockRecordDao.insert 内部的唯一键约束负责。
     */
    @PostMapping("/out-of-stock")
    public ResponseEntity<?> addOutOfStockRecord(@RequestBody AddOutOfStockReq req) {
        try {
            if (req == null || req.getBookId() == null || req.getBookId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("书号不能为空"));
            }
            if (req.getRequiredQuantity() == null || req.getRequiredQuantity() <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("需求数量必须为正整数"));
            }
            OutOfStockRecord record = new OutOfStockRecord();
            record.setBookId(req.getBookId().trim());
            record.setRequiredQuantity(req.getRequiredQuantity());
            record.setRecordDate(LocalDate.now());
            record.setSource("MANUAL");
            record.setStatus("PENDING");
            record.setPriority(req.getPriority() != null ? req.getPriority() : 1);
            long id = outOfStockRecordDao.insert(record);
            OutOfStockRecord saved = outOfStockRecordDao.findById(id);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 查询顾客缺书登记（未付款且待处理）。
     */
    @GetMapping("/customer-requests")
    public ResponseEntity<List<CustomerOutOfStockRequest>> listCustomerRequests() throws SQLException {
        return ResponseEntity.ok(customerReqDao.findPendingUnpaid());
    }

    /**
     * 管理员接受顾客缺书登记：生成缺书记录，并把订单状态从 OUT_OF_STOCK_PENDING 改为 PENDING_PAYMENT。
     */
    @PostMapping("/customer-requests/{requestId}/accept")
    public ResponseEntity<?> acceptCustomerRequest(@PathVariable("requestId") long requestId) {
        try {
            CustomerOutOfStockRequest req = findCustomerRequestById(requestId);
            if (req == null) {
                return ResponseEntity.notFound().build();
            }
            OutOfStockRecord record = new OutOfStockRecord();
            record.setBookId(req.getBookId());
            record.setRequiredQuantity(req.getRequestedQty());
            record.setRecordDate(LocalDate.now());
            record.setSource("CUSTOMER_REQUEST");
            record.setRelatedCustomerId(req.getCustomerId());
            record.setStatus("PENDING");
            record.setPriority(1);
            long rid = outOfStockRecordDao.insert(record);

            customerReqDao.updateProcessedStatus(requestId, "ACCEPTED", rid);
            salesOrderDao.updateStatusAndPaymentTime(req.getOrderId(), "PENDING_PAYMENT", null);

            // 写入一条缺书登记处理结果通知，提示顾客尽快付款
            try {
                CustomerNotification n = new CustomerNotification();
                n.setCustomerId(req.getCustomerId());
                n.setOrderId(req.getOrderId());
                n.setType("OUT_OF_STOCK");
                n.setTitle("缺书登记处理结果");
                n.setContent("您的订单（" + req.getOrderId() + "）的缺货登记已通过，请抓紧付款。");
                n.setReadFlag(false);
                notificationDao.insert(n);
            } catch (Exception ignore) {
                // 单条通知写入失败不影响主流程
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 管理员拒绝顾客缺书登记：不生成缺书记录，并把订单状态从 OUT_OF_STOCK_PENDING 改为 CANCELLED。
     */
    @PostMapping("/customer-requests/{requestId}/reject")
    public ResponseEntity<?> rejectCustomerRequest(@PathVariable("requestId") long requestId) {
        try {
            CustomerOutOfStockRequest req = findCustomerRequestById(requestId);
            if (req == null) {
                return ResponseEntity.notFound().build();
            }
            customerReqDao.updateProcessedStatus(requestId, "REJECTED", null);
            salesOrderDao.updateStatusAndPaymentTime(req.getOrderId(), "CANCELLED", null);

            // 写入一条缺书登记处理结果通知，提示订单已取消
            try {
                CustomerNotification n = new CustomerNotification();
                n.setCustomerId(req.getCustomerId());
                n.setOrderId(req.getOrderId());
                n.setType("OUT_OF_STOCK");
                n.setTitle("缺书登记处理结果");
                n.setContent("您的订单（" + req.getOrderId() + "）的缺货登记未通过，订单已取消。");
                n.setReadFlag(false);
                notificationDao.insert(n);
            } catch (Exception ignore) {
                // 单条通知写入失败不影响主流程
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * DAO 当前未提供按主键查询的方法，这里通过遍历 pending 列表模拟 findById，
     * 与 JavaFX 中 createCustomerOutOfStockRequestTable 的使用场景等价（只操作 PENDING 记录）。
     */
    private CustomerOutOfStockRequest findCustomerRequestById(long requestId) throws SQLException {
        for (CustomerOutOfStockRequest r : customerReqDao.findPendingUnpaid()) {
            if (r.getRequestId() == requestId) {
                return r;
            }
        }
        return null;
    }

    /**
     * 根据多条缺书记录批量生成一张采购单。
     */
    @PostMapping("/orders/from-out-of-stock")
    public ResponseEntity<?> createPurchaseFromOutOfStock(@RequestBody CreateFromOutOfStockReq req) {
        try {
            if (req == null || req.getRecordIds() == null || req.getRecordIds().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("缺书记录列表不能为空"));
            }
            if (req.getSupplierId() == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("供应商ID不能为空"));
            }
            LocalDate expectedDate = req.getExpectedDate();
            String buyer = req.getBuyer() != null ? req.getBuyer() : "";
            long poId = purchaseService.createPurchaseOrderFromOutOfStock(
                    req.getRecordIds(), req.getSupplierId(), expectedDate, buyer);
            PurchaseOrder po = purchaseOrderDao.findById(poId);
            return ResponseEntity.ok(po);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 查询所有采购单。
     */
    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseOrder>> listPurchaseOrders() throws SQLException {
        return ResponseEntity.ok(purchaseOrderDao.findAll());
    }

    /**
     * 查询采购单详情（含明细）。
     */
    @GetMapping("/orders/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderDetailResp> purchaseOrderDetail(
            @PathVariable("purchaseOrderId") long purchaseOrderId) throws SQLException {
        PurchaseOrder order = purchaseOrderDao.findById(purchaseOrderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        List<PurchaseOrderItem> items = purchaseOrderDao.findItemsByOrderId(purchaseOrderId);
        PurchaseOrderDetailResp resp = new PurchaseOrderDetailResp();
        resp.setOrder(order);
        resp.setItems(items != null ? items : new ArrayList<>());
        return ResponseEntity.ok(resp);
    }

    /**
     * 采购单到货：调用 PurchaseService.receiveGoods。
     */
    @PostMapping("/orders/{purchaseOrderId}/receive")
    public ResponseEntity<?> receiveGoods(@PathVariable("purchaseOrderId") long purchaseOrderId) {
        try {
            purchaseService.receiveGoods(purchaseOrderId);
            PurchaseOrder updated = purchaseOrderDao.findById(purchaseOrderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    public static class CreateFromOutOfStockReq {
        private List<Long> recordIds;
        private Long supplierId;
        private LocalDate expectedDate;
        private String buyer;

        public List<Long> getRecordIds() {
            return recordIds;
        }

        public void setRecordIds(List<Long> recordIds) {
            this.recordIds = recordIds;
        }

        public Long getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(Long supplierId) {
            this.supplierId = supplierId;
        }

        public LocalDate getExpectedDate() {
            return expectedDate;
        }

        public void setExpectedDate(LocalDate expectedDate) {
            this.expectedDate = expectedDate;
        }

        public String getBuyer() {
            return buyer;
        }

        public void setBuyer(String buyer) {
            this.buyer = buyer;
        }
    }

    public static class AddOutOfStockReq {
        private String bookId;
        private Integer requiredQuantity;
        private Integer priority;

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public Integer getRequiredQuantity() {
            return requiredQuantity;
        }

        public void setRequiredQuantity(Integer requiredQuantity) {
            this.requiredQuantity = requiredQuantity;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }

    public static class PurchaseOrderDetailResp {
        private PurchaseOrder order;
        private List<PurchaseOrderItem> items;

        public PurchaseOrder getOrder() {
            return order;
        }

        public void setOrder(PurchaseOrder order) {
            this.order = order;
        }

        public List<PurchaseOrderItem> getItems() {
            return items;
        }

        public void setItems(List<PurchaseOrderItem> items) {
            this.items = items;
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



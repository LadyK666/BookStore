package com.bookstore.service;

import com.bookstore.dao.InventoryDao;
import com.bookstore.dao.OutOfStockRecordDao;
import com.bookstore.dao.PurchaseOrderDao;
import com.bookstore.dao.SupplyDao;
import com.bookstore.model.OutOfStockRecord;
import com.bookstore.model.PurchaseOrder;
import com.bookstore.model.PurchaseOrderItem;
import com.bookstore.model.Supply;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购业务服务
 */
public class PurchaseService {

    private final PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final OutOfStockRecordDao outOfStockRecordDao = new OutOfStockRecordDao();
    private final SupplyDao supplyDao = new SupplyDao();

    /**
     * 处理采购单到货：
     * 1. 增加库存
     * 2. 更新关联的缺书记录状态为 COMPLETED
     * 3. 更新采购单状态为 COMPLETED
     */
    public void receiveGoods(long purchaseOrderId) throws SQLException {
        PurchaseOrder order = purchaseOrderDao.findById(purchaseOrderId);
        if (order == null) {
            throw new IllegalArgumentException("采购单不存在: " + purchaseOrderId);
        }
        if ("COMPLETED".equals(order.getStatus())) {
            System.out.println("采购单已完成，无需重复处理");
            return;
        }

        List<PurchaseOrderItem> items = purchaseOrderDao.findItemsByOrderId(purchaseOrderId);

        for (PurchaseOrderItem item : items) {
            // 增加库存
            inventoryDao.increaseQuantity(item.getBookId(), item.getPurchaseQuantity());
            System.out.printf("  书号 %s 库存增加 %d%n", item.getBookId(), item.getPurchaseQuantity());

            // 如果关联了缺书记录，安全地将其标记为 COMPLETED（避免唯一约束冲突）
            if (item.getRelatedOutOfStockId() != null) {
                outOfStockRecordDao.completeRecordSafely(item.getRelatedOutOfStockId());
                System.out.printf("  缺书记录 %d 状态更新为 COMPLETED（或已合并）%n", item.getRelatedOutOfStockId());
            }
        }

        // 更新采购单状态
        purchaseOrderDao.updateStatus(purchaseOrderId, "COMPLETED");
        System.out.println("采购单 " + purchaseOrderId + " 已完成到货处理");
    }

    /**
     * 根据多条缺书记录批量生成一张采购单：
     * - 所有记录必须使用同一供应商
     * - 采购数量取缺书记录的 required_quantity
     * - 单价优先使用供货关系表 supply 中该供应商的供货价；若不存在则抛出异常
     * - 生成采购单后，将缺书记录状态更新为 PURCHASING
     *
     * @param recordIds   选中的缺书记录 ID 列表（要求非空）
     * @param supplierId  供应商 ID
     * @param expectedDate 期望到货日期，允许为 null
     * @param buyer       采购员姓名
     * @return 新建采购单的 ID
     */
    public long createPurchaseOrderFromOutOfStock(List<Long> recordIds,
                                                  long supplierId,
                                                  LocalDate expectedDate,
                                                  String buyer) throws SQLException {
        if (recordIds == null || recordIds.isEmpty()) {
            throw new IllegalArgumentException("缺书记录列表不能为空");
        }

        // 查询所有选中的缺书记录
        List<OutOfStockRecord> records = new ArrayList<>();
        for (Long id : recordIds) {
            OutOfStockRecord r = outOfStockRecordDao.findById(id);
            if (r == null) {
                throw new IllegalArgumentException("缺书记录不存在: " + id);
            }
            if (!"PENDING".equals(r.getStatus())) {
                throw new IllegalStateException("缺书记录状态必须为 PENDING，record_id=" + id);
            }
            records.add(r);
        }

        // 为每本书找到该供应商的供货价
        List<PurchaseOrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OutOfStockRecord r : records) {
            String bookId = r.getBookId();
            List<Supply> suppliesForBook = supplyDao.findByBookId(bookId);
            Supply matched = null;
            for (Supply s : suppliesForBook) {
                if (s.getSupplierId() != null && s.getSupplierId() == supplierId) {
                    matched = s;
                    if (s.isPrimary()) {
                        break;
                    }
                }
            }
            if (matched == null) {
                throw new IllegalStateException("供应商 " + supplierId + " 对书号 " + bookId + " 未配置供货价");
            }

            BigDecimal price = matched.getSupplyPrice();
            int quantity = r.getRequiredQuantity();

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setBookId(bookId);
            item.setPurchaseQuantity(quantity);
            item.setPurchasePrice(price);
            item.setRelatedOutOfStockId(r.getRecordId());
            items.add(item);

            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        // 构造采购单主表
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setCreateDate(LocalDate.now());
        order.setExpectedDate(expectedDate);
        order.setBuyer(buyer);
        order.setEstimatedAmount(totalAmount);
        order.setStatus("ISSUED");

        long poId = purchaseOrderDao.createPurchaseOrder(order, items);

        // 将相关缺书记录状态改为 PURCHASING
        for (OutOfStockRecord r : records) {
            outOfStockRecordDao.updateStatus(r.getRecordId(), "PURCHASING");
        }

        return poId;
    }
}


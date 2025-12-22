package com.bookstore;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.service.PurchaseService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试缺书记录 -> 采购单 -> 到货 -> 库存增加 完整流程
 */
public class TestPurchase {
    public static void main(String[] args) throws SQLException {
        OutOfStockRecordDao oosDao = new OutOfStockRecordDao();
        PurchaseOrderDao poDao = new PurchaseOrderDao();
        InventoryDao invDao = new InventoryDao();
        PurchaseService purchaseService = new PurchaseService();

        // 1. 查看 B001 当前库存
        int beforeQty = invDao.getQuantity("B001");
        System.out.println("操作前 B001 库存：" + beforeQty);

        // 2. 创建一条缺书记录（手动登记）
        OutOfStockRecord oos = new OutOfStockRecord();
        oos.setBookId("B001");
        oos.setRequiredQuantity(30);
        oos.setRecordDate(LocalDate.now());
        oos.setSource("MANUAL");
        oos.setStatus("PENDING");
        oos.setPriority(1);
        long oosId = oosDao.insert(oos);
        System.out.println("创建缺书记录，record_id = " + oosId);

        // 3. 查看待处理缺书记录
        System.out.println("当前待处理缺书记录：");
        for (OutOfStockRecord r : oosDao.findPending()) {
            System.out.println("  " + r);
        }

        // 4. 根据缺书记录创建采购单（向供应商 1 采购）
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplierId(1L);  // 假设供应商 1 存在
        po.setCreateDate(LocalDate.now());
        po.setExpectedDate(LocalDate.now().plusDays(7));
        po.setBuyer("采购员小王");
        po.setEstimatedAmount(new BigDecimal("1200.00"));  // 30 * 40 = 1200
        po.setStatus("ISSUED");

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setBookId("B001");
        poItem.setPurchaseQuantity(30);
        poItem.setPurchasePrice(new BigDecimal("40.00"));
        poItem.setRelatedOutOfStockId(oosId);

        List<PurchaseOrderItem> items = new ArrayList<>();
        items.add(poItem);

        long poId = poDao.createPurchaseOrder(po, items);
        System.out.println("创建采购单，purchase_order_id = " + poId);

        // 更新缺书记录状态为"采购中"
        oosDao.updateStatus(oosId, "PURCHASING");

        // 5. 查看采购单和明细
        PurchaseOrder savedPO = poDao.findById(poId);
        System.out.println("采购单主表：" + savedPO);
        System.out.println("采购明细：");
        for (PurchaseOrderItem item : poDao.findItemsByOrderId(poId)) {
            System.out.println("  " + item);
        }

        // 6. 模拟到货：调用 PurchaseService.receiveGoods
        System.out.println("\n--- 模拟到货处理 ---");
        purchaseService.receiveGoods(poId);

        // 7. 查看处理后的库存
        int afterQty = invDao.getQuantity("B001");
        System.out.println("\n操作后 B001 库存：" + afterQty + "（增加了 " + (afterQty - beforeQty) + "）");

        // 8. 查看缺书记录状态
        OutOfStockRecord updatedOos = oosDao.findById(oosId);
        System.out.println("缺书记录状态：" + updatedOos.getStatus());

        // 9. 查看采购单状态
        PurchaseOrder updatedPO = poDao.findById(poId);
        System.out.println("采购单状态：" + updatedPO.getStatus());
    }
}


package com.bookstore.web.controller;

import com.bookstore.dao.InventoryDao;
import com.bookstore.dao.OutOfStockRecordDao;
import com.bookstore.model.Inventory;
import com.bookstore.model.OutOfStockRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * 管理员端 - 库存管理相关接口。
 *
 * 对应 AdminView.showInventoryManagement / loadInventory / adjustInventory / checkAndCreateLowStockRecord：
 * - 查询全部库存记录；
 * - 调整安全库存；
 * - 增减库存数量；
 * - 当库存低于安全库存时，自动生成缺书记录（LOW_STOCK）。
 */
@RestController
@RequestMapping("/api/admin/inventory")
@CrossOrigin
public class AdminInventoryController {

    private final InventoryDao inventoryDao = new InventoryDao();
    private final OutOfStockRecordDao outOfStockRecordDao = new OutOfStockRecordDao();

    /**
     * 查询所有库存记录。
     */
    @GetMapping
    public ResponseEntity<List<Inventory>> list() throws SQLException {
        return ResponseEntity.ok(inventoryDao.findAll());
    }

    /**
     * 更新指定书目的安全库存。
     */
    @PostMapping("/{bookId}/safety-stock")
    public ResponseEntity<?> updateSafetyStock(@PathVariable("bookId") String bookId,
                                               @RequestBody SafetyStockReq req) {
        try {
            if (req == null || req.getSafetyStock() == null || req.getSafetyStock() < 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("安全库存必须是非负整数"));
            }
            inventoryDao.updateSafetyStock(bookId, req.getSafetyStock());
            checkAndCreateLowStockRecord(bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 调整库存数量：delta > 0 表示增加，delta < 0 表示减少。
     */
    @PostMapping("/{bookId}/adjust")
    public ResponseEntity<?> adjustInventory(@PathVariable("bookId") String bookId,
                                             @RequestBody AdjustReq req) {
        try {
            if (req == null || req.getDelta() == null || req.getDelta() == 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("调整数量不能为空或 0"));
            }
            int delta = req.getDelta();
            if (delta > 0) {
                inventoryDao.increaseQuantity(bookId, delta);
            } else {
                int updated = inventoryDao.decreaseQuantity(bookId, -delta);
                if (updated == 0) {
                    return ResponseEntity.badRequest().body(new ErrorResp("库存不足，无法减少这么多数量"));
                }
            }
            checkAndCreateLowStockRecord(bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 当某本书库存低于安全库存时，自动生成（或累加）一条缺书记录（source=LOW_STOCK, status=PENDING）。
     * 逻辑与 AdminView.checkAndCreateLowStockRecord 保持一致。
     */
    private void checkAndCreateLowStockRecord(String bookId) throws SQLException {
        int qty = inventoryDao.getQuantity(bookId);
        int safety = inventoryDao.getSafetyStock(bookId);
        if (safety > 0 && qty < safety) {
            OutOfStockRecord record = new OutOfStockRecord();
            record.setBookId(bookId);
            // 按“缺口量”登记
            record.setRequiredQuantity(safety - qty);
            record.setRecordDate(LocalDate.now());
            record.setSource("LOW_STOCK");
            record.setStatus("PENDING");
            record.setPriority(1);
            outOfStockRecordDao.insert(record);
        }
    }

    public static class SafetyStockReq {
        private Integer safetyStock;

        public Integer getSafetyStock() {
            return safetyStock;
        }

        public void setSafetyStock(Integer safetyStock) {
            this.safetyStock = safetyStock;
        }
    }

    public static class AdjustReq {
        private Integer delta;

        public Integer getDelta() {
            return delta;
        }

        public void setDelta(Integer delta) {
            this.delta = delta;
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



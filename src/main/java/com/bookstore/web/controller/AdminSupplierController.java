package com.bookstore.web.controller;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.SupplyDao;
import com.bookstore.dao.SupplierDao;
import com.bookstore.model.Book;
import com.bookstore.model.Supplier;
import com.bookstore.model.Supply;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员端 - 供应商管理相关接口。
 *
 * 对应 AdminView.showSupplierManagement / showAddSupplierDialog：
 * - 查询全部供应商；
 * - 新增供应商；
 * - 查看供应商供货清单（含书目信息）；
 * - 编辑供应商供货关系（供价、提前期、主供货商标记）。
 */
@RestController
@RequestMapping("/api/admin/suppliers")
@CrossOrigin
public class AdminSupplierController {

    private final SupplierDao supplierDao = new SupplierDao();
    private final SupplyDao supplyDao = new SupplyDao();
    private final BookDao bookDao = new BookDao();

    @GetMapping
    public ResponseEntity<List<Supplier>> listSuppliers() throws SQLException {
        return ResponseEntity.ok(supplierDao.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addSupplier(@RequestBody AddSupplierReq req) {
        try {
            if (req == null || req.getSupplierName() == null || req.getSupplierName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("供应商名称不能为空"));
            }
            Supplier s = new Supplier();
            s.setSupplierName(req.getSupplierName().trim());
            s.setContactPerson(req.getContactPerson() != null ? req.getContactPerson().trim() : null);
            s.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
            s.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
            s.setAddress(req.getAddress() != null ? req.getAddress().trim() : null);
            s.setPaymentTerms(req.getPaymentTerms() != null ? req.getPaymentTerms().trim() : null);
            s.setCooperationStatus("ACTIVE");
            supplierDao.insert(s);
            return ResponseEntity.ok(s);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 更新供应商信息
     */
    @PutMapping("/{supplierId}")
    public ResponseEntity<?> updateSupplier(@PathVariable("supplierId") long supplierId,
                                             @RequestBody AddSupplierReq req) {
        try {
            Supplier existing = supplierDao.findById(supplierId);
            if (existing == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("供应商不存在"));
            }
            if (req == null || req.getSupplierName() == null || req.getSupplierName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("供应商名称不能为空"));
            }
            Supplier s = new Supplier();
            s.setSupplierId(supplierId);
            s.setSupplierName(req.getSupplierName().trim());
            s.setContactPerson(req.getContactPerson() != null ? req.getContactPerson().trim() : null);
            s.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
            s.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
            s.setAddress(req.getAddress() != null ? req.getAddress().trim() : null);
            s.setPaymentTerms(req.getPaymentTerms() != null ? req.getPaymentTerms().trim() : null);
            s.setCooperationStatus(existing.getCooperationStatus()); // 保持原有状态
            supplierDao.update(s);
            return ResponseEntity.ok(supplierDao.findById(supplierId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{supplierId}")
    public ResponseEntity<?> deleteSupplier(@PathVariable("supplierId") long supplierId) {
        try {
            Supplier existing = supplierDao.findById(supplierId);
            if (existing == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("供应商不存在"));
            }
            // 检查是否有供货关系
            List<Supply> supplies = supplyDao.findBySupplierId(supplierId);
            if (supplies != null && !supplies.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("该供应商存在供货关系，无法删除。请先删除所有供货关系后再删除供应商。"));
            }
            supplierDao.delete(supplierId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 获取供应商的供货清单（含书目信息）。
     */
    @GetMapping("/{supplierId}/supplies")
    public ResponseEntity<List<SupplyDetailResp>> getSupplierSupplies(@PathVariable("supplierId") long supplierId)
            throws SQLException {
        List<Supply> supplies = supplyDao.findBySupplierId(supplierId);
        List<SupplyDetailResp> result = new ArrayList<>();
        for (Supply supply : supplies) {
            SupplyDetailResp resp = new SupplyDetailResp();
            resp.setSupplierId(supply.getSupplierId());
            resp.setBookId(supply.getBookId());
            resp.setSupplyPrice(supply.getSupplyPrice());
            resp.setLeadTimeDays(supply.getLeadTimeDays());
            resp.setPrimary(supply.isPrimary());
            // 查询书目信息
            Book book = bookDao.findById(supply.getBookId());
            if (book != null) {
                resp.setBookTitle(book.getTitle());
                resp.setBookIsbn(book.getIsbn());
                resp.setBookPublisher(book.getPublisher());
                resp.setBookPrice(book.getPrice());
                resp.setBookSeriesFlag(book.isSeriesFlag());
                resp.setBookParentBookId(book.getParentBookId());
            }
            result.add(resp);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 为供应商添加新的供货关系。
     */
    @PostMapping("/{supplierId}/supplies")
    public ResponseEntity<?> addSupply(@PathVariable("supplierId") long supplierId,
                                       @RequestBody AddSupplyReq req) {
        try {
            if (req.getBookId() == null || req.getBookId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("书号不能为空"));
            }
            // 检查书目是否存在
            Book book = bookDao.findById(req.getBookId().trim());
            if (book == null) {
                return ResponseEntity.badRequest().body(new ErrorResp("书目不存在，书号：" + req.getBookId()));
            }
            Supply supply = new Supply();
            supply.setSupplierId(supplierId);
            supply.setBookId(req.getBookId().trim());
            supply.setSupplyPrice(req.getSupplyPrice());
            supply.setLeadTimeDays(req.getLeadTimeDays());
            supply.setPrimary(req.isPrimary() != null && req.isPrimary());
            supplyDao.insert(supply);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 更新供应商的供货关系（供价、提前期、主供货商标记）。
     */
    @PutMapping("/{supplierId}/supplies/{bookId}")
    public ResponseEntity<?> updateSupply(@PathVariable("supplierId") long supplierId,
                                          @PathVariable("bookId") String bookId,
                                          @RequestBody UpdateSupplyReq req) {
        try {
            Supply supply = new Supply();
            supply.setSupplierId(supplierId);
            supply.setBookId(bookId);
            supply.setSupplyPrice(req.getSupplyPrice());
            supply.setLeadTimeDays(req.getLeadTimeDays());
            if (req.getPrimary() != null) {
                supply.setPrimary(req.getPrimary());
                // 如果设置为主供货商，需要先将该书的其他供货关系 is_primary 置为 0
                if (req.getPrimary()) {
                    List<Supply> allSupplies = supplyDao.findByBookId(bookId);
                    for (Supply s : allSupplies) {
                        if (!s.getSupplierId().equals(supplierId) && s.isPrimary()) {
                            Supply updateOther = new Supply();
                            updateOther.setSupplierId(s.getSupplierId());
                            updateOther.setBookId(bookId);
                            updateOther.setSupplyPrice(s.getSupplyPrice());
                            updateOther.setLeadTimeDays(s.getLeadTimeDays());
                            updateOther.setPrimary(false);
                            supplyDao.update(updateOther);
                        }
                    }
                }
            }
            supplyDao.update(supply);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    /**
     * 删除供应商的供货关系。
     */
    @DeleteMapping("/{supplierId}/supplies/{bookId}")
    public ResponseEntity<?> deleteSupply(@PathVariable("supplierId") long supplierId,
                                           @PathVariable("bookId") String bookId) {
        try {
            supplyDao.delete(supplierId, bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    public static class AddSupplierReq {
        private String supplierName;
        private String contactPerson;
        private String phone;
        private String email;
        private String address;
        private String paymentTerms;

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public void setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPaymentTerms() {
            return paymentTerms;
        }

        public void setPaymentTerms(String paymentTerms) {
            this.paymentTerms = paymentTerms;
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

    public static class SupplyDetailResp {
        private Long supplierId;
        private String bookId;
        private String bookTitle;
        private String bookIsbn;
        private String bookPublisher;
        private BigDecimal bookPrice;
        private Boolean bookSeriesFlag;
        private String bookParentBookId;
        private BigDecimal supplyPrice;
        private Integer leadTimeDays;
        private boolean primary;

        public Long getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(Long supplierId) {
            this.supplierId = supplierId;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public String getBookIsbn() {
            return bookIsbn;
        }

        public void setBookIsbn(String bookIsbn) {
            this.bookIsbn = bookIsbn;
        }

        public String getBookPublisher() {
            return bookPublisher;
        }

        public void setBookPublisher(String bookPublisher) {
            this.bookPublisher = bookPublisher;
        }

        public BigDecimal getBookPrice() {
            return bookPrice;
        }

        public void setBookPrice(BigDecimal bookPrice) {
            this.bookPrice = bookPrice;
        }

        public BigDecimal getSupplyPrice() {
            return supplyPrice;
        }

        public void setSupplyPrice(BigDecimal supplyPrice) {
            this.supplyPrice = supplyPrice;
        }

        public Integer getLeadTimeDays() {
            return leadTimeDays;
        }

        public void setLeadTimeDays(Integer leadTimeDays) {
            this.leadTimeDays = leadTimeDays;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public Boolean getBookSeriesFlag() {
            return bookSeriesFlag;
        }

        public void setBookSeriesFlag(Boolean bookSeriesFlag) {
            this.bookSeriesFlag = bookSeriesFlag;
        }

        public String getBookParentBookId() {
            return bookParentBookId;
        }

        public void setBookParentBookId(String bookParentBookId) {
            this.bookParentBookId = bookParentBookId;
        }
    }

    public static class AddSupplyReq {
        private String bookId;
        private BigDecimal supplyPrice;
        private Integer leadTimeDays;
        private Boolean primary;

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public BigDecimal getSupplyPrice() {
            return supplyPrice;
        }

        public void setSupplyPrice(BigDecimal supplyPrice) {
            this.supplyPrice = supplyPrice;
        }

        public Integer getLeadTimeDays() {
            return leadTimeDays;
        }

        public void setLeadTimeDays(Integer leadTimeDays) {
            this.leadTimeDays = leadTimeDays;
        }

        public Boolean isPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }
    }

    public static class UpdateSupplyReq {
        private BigDecimal supplyPrice;
        private Integer leadTimeDays;
        private Boolean primary;

        public BigDecimal getSupplyPrice() {
            return supplyPrice;
        }

        public void setSupplyPrice(BigDecimal supplyPrice) {
            this.supplyPrice = supplyPrice;
        }

        public Integer getLeadTimeDays() {
            return leadTimeDays;
        }

        public void setLeadTimeDays(Integer leadTimeDays) {
            this.leadTimeDays = leadTimeDays;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }
    }
}



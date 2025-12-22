package com.bookstore.web.controller;

import com.bookstore.dao.CustomerAddressDao;
import com.bookstore.model.CustomerAddress;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * 顾客端 - 收货地址管理接口。
 * 逻辑对应 CustomerView.showAddressManagement / loadAddresses / showAddAddressDialog：
 * - 列出顾客的全部地址
 * - 新增地址（可设置为默认）
 * - 将某条地址设为默认
 * - 删除地址
 */
@RestController
@RequestMapping("/api/customer/{customerId}/addresses")
@CrossOrigin
public class CustomerAddressController {

    private final CustomerAddressDao addressDao = new CustomerAddressDao();

    @GetMapping
    public ResponseEntity<List<CustomerAddress>> list(@PathVariable("customerId") long customerId) throws SQLException {
        return ResponseEntity.ok(addressDao.findByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable("customerId") long customerId,
                                 @RequestBody AddressCreateReq req) {
        try {
            if (req.getReceiver() == null || req.getReceiver().trim().isEmpty()
                    || req.getDetail() == null || req.getDetail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResp("收件人和详细地址不能为空"));
            }
            CustomerAddress addr = new CustomerAddress();
            addr.setCustomerId(customerId);
            addr.setReceiver(req.getReceiver().trim());
            addr.setPhone(req.getPhone());
            addr.setProvince(req.getProvince());
            addr.setCity(req.getCity());
            addr.setDistrict(req.getDistrict());
            addr.setDetail(req.getDetail().trim());
            addr.setDefault(Boolean.TRUE.equals(req.getIsDefault()));
            addressDao.insert(addr);
            return ResponseEntity.ok(addr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @PostMapping("/{addressId}/default")
    public ResponseEntity<?> setDefault(@PathVariable("customerId") long customerId,
                                        @PathVariable("addressId") long addressId) {
        try {
            addressDao.updateDefault(customerId, addressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> delete(@PathVariable("addressId") long addressId) {
        try {
            addressDao.delete(addressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResp(e.getMessage()));
        }
    }

    public static class AddressCreateReq {
        private String receiver;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String detail;
        private Boolean isDefault;

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
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



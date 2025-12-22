package com.bookstore.model;

/**
 * 客户地址实体类，对应表 customer_address。
 */
public class CustomerAddress {

    private Long addressId;
    private Long customerId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private boolean isDefault;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    /**
     * 用于界面展示的简短地址字符串。
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(receiver != null ? receiver : "");
        if (phone != null && !phone.isEmpty()) {
            sb.append(" / ").append(phone);
        }
        sb.append(" - ");
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (district != null) sb.append(district);
        if (detail != null) sb.append(detail);
        if (isDefault) {
            sb.append(" [默认]");
        }
        return sb.toString();
    }
}



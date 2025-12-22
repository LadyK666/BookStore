package com.bookstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户实体类，对应表：customer。
 * 字段设计参考数据库设计文档中的 Customer 实体。
 */
public class Customer {

    private Long customerId;
    private String username;
    private String passwordHash;
    private String realName;
    private String mobilePhone;
    private String email;
    private BigDecimal accountBalance;
    private BigDecimal totalConsumption;
    private LocalDateTime registrationTime;
    private String accountStatus;
    private Integer creditLevelId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public BigDecimal getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(BigDecimal totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Integer getCreditLevelId() {
        return creditLevelId;
    }

    public void setCreditLevelId(Integer creditLevelId) {
        this.creditLevelId = creditLevelId;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", email='" + email + '\'' +
                ", accountBalance=" + accountBalance +
                ", totalConsumption=" + totalConsumption +
                ", registrationTime=" + registrationTime +
                ", accountStatus='" + accountStatus + '\'' +
                ", creditLevelId=" + creditLevelId +
                '}';
    }
}



package com.bookstore.model;

import java.math.BigDecimal;

/**
 * 信用等级实体类，对应表：credit_level。
 */
public class CreditLevel {

    private Integer levelId;
    private String levelName;
    private BigDecimal discountRate;
    private boolean allowOverdraft;
    private BigDecimal overdraftLimit;
    private String upgradeCondition;

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public boolean isAllowOverdraft() {
        return allowOverdraft;
    }

    public void setAllowOverdraft(boolean allowOverdraft) {
        this.allowOverdraft = allowOverdraft;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public String getUpgradeCondition() {
        return upgradeCondition;
    }

    public void setUpgradeCondition(String upgradeCondition) {
        this.upgradeCondition = upgradeCondition;
    }

    @Override
    public String toString() {
        return "CreditLevel{" +
                "levelId=" + levelId +
                ", levelName='" + levelName + '\'' +
                ", discountRate=" + discountRate +
                ", allowOverdraft=" + allowOverdraft +
                ", overdraftLimit=" + overdraftLimit +
                ", upgradeCondition='" + upgradeCondition + '\'' +
                '}';
    }
}



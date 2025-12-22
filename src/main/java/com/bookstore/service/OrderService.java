package com.bookstore.service;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单相关业务服务。
 * 本阶段实现：根据信用等级规则完成“付款与信用校验”，并更新账户余额与订单状态。
 *
 * 业务规则（简化自设计文档）：
 *  - 一、二级：不允许透支，要求 余额 >= 应付金额；
 *  - 三、四级：允许透支，要求 余额 + 透支额度 >= 应付金额；
 *  - 五级：允许透支，透支额度为 -1 视为无限额，只要系统允许即可（本实验中直接通过）。
 */
public class OrderService {

    private final CustomerDao customerDao = new CustomerDao();
    private final CreditLevelDao creditLevelDao = new CreditLevelDao();
    private final SalesOrderDao salesOrderDao = new SalesOrderDao();

    /**
     * 对指定订单执行“付款 + 信用校验”：
     *  - 根据订单找到客户与其信用等级；
     *  - 按规则校验是否有足够支付能力；
     *  - 扣减客户余额（可能为负，表示透支）；
     *  - 更新订单状态为 PENDING_SHIPMENT，并记录 payment_time。
     *
     * @throws IllegalStateException 支付能力不足时抛出
     */
    public void payOrder(long orderId) throws SQLException {
        // 为确保账户扣款与订单状态更新在同一事务中，这里手动管理连接与事务。
        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // 1. 读取订单
                SalesOrder order = salesOrderDao.findOrderById(orderId);
                if (order == null) {
                    throw new IllegalStateException("订单不存在，orderId=" + orderId);
                }

                String originalStatus = order.getOrderStatus();

                // 2. 读取客户与信用等级
                Customer customer = customerDao.findById(order.getCustomerId());
                if (customer == null) {
                    throw new IllegalStateException("关联客户不存在，customerId=" + order.getCustomerId());
                }
                CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
                if (level == null) {
                    throw new IllegalStateException("客户信用等级不存在，creditLevelId=" + customer.getCreditLevelId());
                }

                BigDecimal payable = order.getPayableAmount();
                BigDecimal balance = customer.getAccountBalance();
                BigDecimal overdraftLimit = level.getOverdraftLimit();

                // 3. 根据信用等级校验支付能力
                boolean allowOverdraft = level.isAllowOverdraft();
                boolean canPay;
                if (!allowOverdraft) {
                    // 一、二级：不允许透支，余额必须 >= 应付金额
                    canPay = balance.compareTo(payable) >= 0;
                } else {
                    if (overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.valueOf(-1)) == 0) {
                        // 五级：透支额度为 -1 视为无限透支
                        canPay = true;
                    } else {
                        // 三、四级：允许透支，余额 + 透支额度 >= 应付金额
                        BigDecimal available = balance.add(overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO);
                        canPay = available.compareTo(payable) >= 0;
                    }
                }

                if (!canPay) {
                    String msg = "支付失败：账户余额与透支额度不足以支付本订单。";
                    msg += "\n应付金额：¥" + payable;
                    msg += "\n当前余额：¥" + balance;
                    if (allowOverdraft) {
                        if (overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.valueOf(-1)) == 0) {
                            msg += "\n透支额度：无限";
                        } else {
                            msg += "\n透支额度：¥" + (overdraftLimit != null ? overdraftLimit : "0");
                        }
                    } else {
                        msg += "\n您的信用等级不允许透支";
                    }
                    throw new IllegalStateException(msg);
                }

                // 4. 扣减余额
                BigDecimal newBalance = balance.subtract(payable);
                // 使用独立 SQL 更新余额和订单状态，为简化实践，这里仍通过 DAO 逐步调用
                customerDao.updateAccountBalance(customer.getCustomerId(), newBalance);

                // 5. 更新累积消费
                customerDao.addTotalConsumption(customer.getCustomerId(), payable);

                // 6. 更新订单状态与支付时间
                salesOrderDao.updateStatusAndPaymentTime(orderId, "PENDING_SHIPMENT", LocalDateTime.now());

                // 7. 若原状态为缺货待确认，则在付款成功后自动生成缺书记录
                if ("OUT_OF_STOCK_PENDING".equals(originalStatus)) {
                    CustomerOutOfStockRequestDao reqDao = new CustomerOutOfStockRequestDao();
                    OutOfStockRecordDao oosDao = new OutOfStockRecordDao();
                    List<CustomerOutOfStockRequest> pendingReqs = reqDao.findPendingByOrderId(orderId);
                    for (CustomerOutOfStockRequest req : pendingReqs) {
                        OutOfStockRecord record = new OutOfStockRecord();
                        record.setBookId(req.getBookId());
                        record.setRequiredQuantity(req.getRequestedQty());
                        record.setRecordDate(java.time.LocalDate.now());
                        record.setSource("CUSTOMER_REQUEST");
                        record.setRelatedCustomerId(order.getCustomerId());
                        record.setStatus("PENDING");
                        record.setPriority(1);
                        long rid = oosDao.insert(record);
                        reqDao.updateProcessedStatus(req.getRequestId(), "ACCEPTED", rid);
                    }
                }

                // 8. 检查并自动升级信用等级
                checkAndUpgradeCreditLevel(customer.getCustomerId());

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                } else if (ex instanceof IllegalStateException) {
                    throw (IllegalStateException) ex;
                } else {
                    throw new RuntimeException(ex);
                }
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * 根据累积消费自动检查并升级客户信用等级。
     * 升级规则：
     * - 二级：累计消费满500元
     * - 三级：累计消费满2000元
     * - 四级：累计消费满5000元
     * - 五级：累计消费满10000元
     */
    private void checkAndUpgradeCreditLevel(long customerId) throws SQLException {
        Customer customer = customerDao.findById(customerId);
        if (customer == null) {
            return;
        }

        BigDecimal totalConsumption = customer.getTotalConsumption();
        if (totalConsumption == null) {
            totalConsumption = BigDecimal.ZERO;
        }

        int currentLevel = customer.getCreditLevelId();
        int newLevel = currentLevel;

        // 根据累积消费确定应该达到的等级
        if (totalConsumption.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            newLevel = 5; // 五级：累计消费满10000元
        } else if (totalConsumption.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            newLevel = 4; // 四级：累计消费满5000元
        } else if (totalConsumption.compareTo(BigDecimal.valueOf(2000)) >= 0) {
            newLevel = 3; // 三级：累计消费满2000元
        } else if (totalConsumption.compareTo(BigDecimal.valueOf(500)) >= 0) {
            newLevel = 2; // 二级：累计消费满500元
        } else {
            newLevel = 1; // 一级：默认等级
        }

        // 如果等级需要提升，则更新
        if (newLevel > currentLevel) {
            customerDao.updateCreditLevel(customerId, newLevel);
        }
    }
}



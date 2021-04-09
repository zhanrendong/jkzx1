package tech.tongyu.bct.cm.product.iov;

public enum RebateTypeEnum {
    /**
     * No rebate
     */
    PAY_NONE,
    /**
     * Pays out the rebate when the barrier is breached
     */
    PAY_WHEN_HIT,
    /**
     * Pays out at option expiry even if the barrier has been breached earlier
     */
    PAY_AT_EXPIRY
}

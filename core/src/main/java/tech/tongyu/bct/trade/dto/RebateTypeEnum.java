package tech.tongyu.bct.trade.dto;

/**
 * How rebate is paid when a triggering event happens (when the spot hits a barrier, for example)
 * 无补偿
 * 敲出即付
 * 到期支付
 */
public enum RebateTypeEnum {
    /**
     * 无补偿
     * No rebate
     */
    PAY_NONE,
    /**
     * 敲出即付
     * Pays out the rebate when the barrier is breached
     */
    PAY_WHEN_HIT,
    /**
     * 到期支付
     * Pays out at option expiry even if the barrier has been breached earlier
     */
    PAY_AT_EXPIRY
}

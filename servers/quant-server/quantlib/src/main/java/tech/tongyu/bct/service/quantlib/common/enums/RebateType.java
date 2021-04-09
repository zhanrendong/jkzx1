package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * How rebate is paid when a triggering event happens (when the spot hits a barrier, for example)
 */
@BctQuantEnum
public enum RebateType {
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
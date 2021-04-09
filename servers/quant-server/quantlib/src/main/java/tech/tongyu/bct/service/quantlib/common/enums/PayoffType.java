package tech.tongyu.bct.service.quantlib.common.enums;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantEnum;

/**
 * Payoff types. Describes how the payment of an option is paid out: in cash or in asset.
 * <ul>
 *     <li>{@link #CASH}</li>
 *     <li>{@link #ASSET}</li>
 * </ul>
 * @author Lu Lu
 * @since 2016-06-24
 */
@BctQuantEnum
public enum PayoffType {
    /**
     * CASH pays out in cash
     */
    CASH,
    /**
     * ASSET pays out in asset. Thus in pricing the payoff has to be converted into cash.
     * For example, if a physically settled digital option pays 1 stock, its value should be S instad of 1.
     */
    ASSET
}
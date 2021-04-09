package tech.tongyu.bct.market.dto;

/**
 * 标的物合约类型
 * <ul>
 *     <li>{@link #SPOT}</li>
 *     <li>{@link #FUTURES}</li>
 *     <li>{@link #STOCK}</li>
 *     <li>{@link #INDEX}</li>
 *     <li>{@link #INDEX_FUTURES}</li>
 *     <li>{@link #BASKET}</li>
 * </ul>
 */
public enum InstrumentTypeEnum {
    /**
     * 现货
     */
    SPOT,
    /**
     * 期货
     */
    FUTURES,
    /**
     * 期货期权
     */
    FUTURES_OPTION,
    /**
     * 股票
     */
    STOCK,
    /**
     * 个股/ETF期权
     */
    STOCK_OPTION,
    /**
     * 指数
     */
    INDEX,
    /**
     * 指数期货
     */
    INDEX_FUTURES,
    /**
     * 股指期权
     */
    INDEX_OPTION,
    /**
     * 篮子
     */
    BASKET
}

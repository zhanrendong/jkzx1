package tech.tongyu.bct.market.dto;

/**
 * 资产类型
 * <ul>
 *     <li>{@link #COMMODITY}</li>
 *     <li>{@link #EQUITY}</li>
 *     <li>{@link #FX}</li>
 *     <li>{@link #RATES}</li>
 *     <li>{@link #CASH}</li>
 * </ul>
 */
public enum AssetClassEnum {
    /**
     * 商品
     */
    COMMODITY,
    /**
     * 权益
     */
    EQUITY,
    /**
     * 外汇
     */
    FX,
    /**
     * 利率
     */
    RATES,
    /**
     * 现金
     */
    CASH
}

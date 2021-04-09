package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.api.doc.BctField;

/**
 * 一般而言，行情分为日内和收盘两大类。根据日内行情产生的波动率曲面/曲线等模型也归类于日内。相应的收盘行情对应的模型归类于收盘。
 * <ul>
 *     <li>{@link #INTRADAY}</li>
 *     <li>{@link #CLOSE}</li>
 * </ul>
 */
public enum InstanceEnum {
    /**
     * 收盘行情/模型
     */
    @BctField(description = "收盘行情/模型")
    CLOSE,
    /**
     * 日内行情/模型
     */
    @BctField(description = "日内行情/模型")
    INTRADAY
}

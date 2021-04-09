package tech.tongyu.bct.model.dto;

import tech.tongyu.bct.common.api.doc.BctField;

/**
 * 模型类型
 * <li>{@link #VOL_SURFACE}</li>
 * <li>{@link #DIVIDEND_CURVE}</li>
 * <li>{@link #RISK_FREE_CURVE}</li>
 */
public enum ModelTypeEnum {
    /**
     * 波动率曲面
     */
    @BctField(description = "波动率曲面")
    VOL_SURFACE,
    /**
     * 分红/融券曲线
     */
    @BctField(description = "分红/融券曲线")
    DIVIDEND_CURVE,
    /**
     * 无风险利率曲线
     */
    @BctField(description = "无风险利率曲线")
    RISK_FREE_CURVE,
    /**
     * 用户自定义插值模型: Model_XY
     */
    @BctField(description = "用户自定义插值模型: Model_XY")
    MODEL_XY
}

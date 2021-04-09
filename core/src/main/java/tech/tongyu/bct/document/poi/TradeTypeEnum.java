package tech.tongyu.bct.document.poi;

import tech.tongyu.bct.common.api.doc.BctField;

public enum TradeTypeEnum {
    @BctField(description = "欧式")
    VANILLA_EUROPEAN,
    @BctField(description = "美式")
    VANILLA_AMERICAN,
    @BctField(description = "二元")
    DIGITAL,
    @BctField(description = "欧式价差")
    VERTICAL_SPREAD,
    @BctField(description = "单鲨")
    BARRIER,
    @BctField(description = "双鲨")
    DOUBLE_SHARK_FIN,
    @BctField(description = "三层阶梯")
    DOUBLE_DIGITAL,
    @BctField(description = "美式双触碰")
    DOUBLE_TOUCH,
    @BctField(description = "美式双不触碰")
    DOUBLE_NO_TOUCH,
    @BctField(description = "二元凹式")
    CONCAVA,
    @BctField(description = "二元凸式")
    CONVEX,
    @BctField(description = "鹰式")
    EAGLE,
    @BctField(description = "区间累积")
    RANGE_ACCRUALS,
    @BctField(description = "四层阶梯")
    TRIPLE_DIGITAL,
    @BctField(description = "自定义产品")
    MODEL_XY,
    @BctField(description = "雪球式AutoCall")
    AUTOCALL,
    @BctField(description = "凤凰式AutoCall")
    AUTOCALL_PHOENIX,
    @BctField(description = "亚式")
    ASIAN,
    @BctField(description = "跨式")
    STRADDLE,
    @BctField(description = "远期")
    FORWARD
}

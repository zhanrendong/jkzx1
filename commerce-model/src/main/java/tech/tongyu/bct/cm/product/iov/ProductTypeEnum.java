package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.core.CMEnumeration;
import tech.tongyu.bct.common.api.doc.BctField;

public enum ProductTypeEnum implements CMEnumeration {
    @BctField(description = "现金")
    CASH("现金"),
    @BctField(description = "其他单资产期权")
    GENERIC_SINGLE_ASSET_OPTION("其他单资产期权"), // a general description of all option types
    @BctField(description = "香草欧式")
    VANILLA_EUROPEAN("香草欧式"),
    @BctField(description = "香草美式")
    VANILLA_AMERICAN("香草美式"),
    @BctField(description = "价差")
    VERTICAL_SPREAD("价差"),
    @BctField(description = "价差-分解")
    VERTICAL_SPREAD_COMBO("价差-分解"),
    @BctField(description = "AutoCall")
    AUTOCALL("AutoCall"),
    @BctField(description = "凤凰式")
    AUTOCALL_PHOENIX("凤凰式"),
    @BctField(description = "篮子")
    BASKET("篮子"),
    @BctField(description = "现货")
    CASH_PRODUCT("现货"),
    @BctField(description = "期货")
    FUTURE("期货"),
    @BctField(description = "二元")
    DIGITAL("二元"),
    @BctField(description = "障碍")
    BARRIER("障碍"),
    @BctField(description = "双鲨")
    DOUBLE_SHARK_FIN("双鲨"),
    @BctField(description = "鹰式")
    EAGLE("鹰式"),
    @BctField(description = "双触碰")
    DOUBLE_TOUCH("双触碰"),
    @BctField(description = "双不触碰")
    DOUBLE_NO_TOUCH("双不触碰"),
    @BctField(description = "二元凹式")
    CONCAVA("二元凹式"),
    @BctField(description = "二元凸式")
    CONVEX("二元凸式"),
    @BctField(description = "三层阶梯")
    DOUBLE_DIGITAL("三层阶梯"),
    @BctField(description = "四层阶梯")
    TRIPLE_DIGITAL("四层阶梯"),
    @BctField(description = "区间累积")
    RANGE_ACCRUALS("区间累积"),
    @BctField(description = "跨式")
    STRADDLE("跨式"),
    @BctField(description = "亚式")
    ASIAN("亚式"),
    @BctField(description = "自定义产品")
    MODEL_XY("自定义产品"),
    @BctField(description = "远期")
    FORWARD("远期"),
    @BctField(description = "价差欧式")
    SPREAD_EUROPEAN("价差欧式"),
    @BctField(description = "价差欧式")
    RATIO_SPREAD_EUROPEAN("价差欧式"),
    @BctField(description = "现金流")
    CASH_FLOW("现金流"),
    @BctField(description = "其他")
    OTHER("其他");

    private String description;

    ProductTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}

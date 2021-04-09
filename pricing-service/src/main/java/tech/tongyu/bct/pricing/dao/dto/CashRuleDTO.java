package tech.tongyu.bct.pricing.dao.dto;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.market.dto.InstanceEnum;

public class CashRuleDTO {
    @BctField(description = "是否贴现")
    private Boolean discounted;
    @BctField(description = "曲线名称")
    private String curveName;
    @BctField(description = "行情",componentClass = InstanceEnum.class)
    private InstanceEnum instance;

    public CashRuleDTO() {
    }

    public CashRuleDTO(Boolean discounted, String curveName, InstanceEnum instance) {
        this.discounted = discounted;
        this.curveName = curveName;
        this.instance = instance;
    }

    public Boolean getDiscounted() {
        return discounted;
    }

    public void setDiscounted(Boolean discounted) {
        this.discounted = discounted;
    }

    public String getCurveName() {
        return curveName;
    }

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }
}

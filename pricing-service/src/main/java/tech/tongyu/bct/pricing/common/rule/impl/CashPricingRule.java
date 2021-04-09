package tech.tongyu.bct.pricing.common.rule.impl;

import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.pricing.common.rule.PricingRule;

public class CashPricingRule implements PricingRule {
    private final Boolean discounted;
    private final String curveName;
    private final InstanceEnum instance;

    public CashPricingRule(Boolean discounted, String curveName, InstanceEnum instance) {
        this.discounted = discounted;
        this.curveName = curveName;
        this.instance = instance;
    }

    public Boolean getDiscounted() {
        return discounted;
    }

    public String getCurveName() {
        return curveName;
    }

    public InstanceEnum getInstance() {
        return instance;
    }
}

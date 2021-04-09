package tech.tongyu.bct.pricing.common.rule.impl;

import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.pricing.common.rule.PricingRule;

public class LinearProductPricingRule implements PricingRule {
    private final InstanceEnum instance;
    private final QuoteFieldEnum field;

    public LinearProductPricingRule(InstanceEnum instance, QuoteFieldEnum field) {
        this.instance = instance;
        this.field = field;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public QuoteFieldEnum getField() {
        return field;
    }
}

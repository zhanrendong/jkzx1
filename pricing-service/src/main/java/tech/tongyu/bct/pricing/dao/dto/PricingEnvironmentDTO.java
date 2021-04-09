package tech.tongyu.bct.pricing.dao.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.List;

public class PricingEnvironmentDTO {
    @BctField(description = "定价环境名称")
    private String pricingEnvironmentId;
    @BctField(description = "现金定价规则",componentClass = CashRuleDTO.class)
    private CashRuleDTO cashRule;
    @BctField(
            description = "现货/期货定价规则",
            isCollection = true,
            componentClass = LinearProductRuleDTO.class)
    private List<LinearProductRuleDTO> linearProductRules;
    @BctField(
            description = "期权定价规则",
            isCollection = true,
            componentClass = SingleAssetOptionRuleDTO.class)
    private List<SingleAssetOptionRuleDTO> singleAssetOptionRules;
    @BctField(description = "定价环境说明")
    private String description;

    public PricingEnvironmentDTO() {
    }

    public PricingEnvironmentDTO(String pricingEnvironmentId,
                                 CashRuleDTO cashRule,
                                 List<LinearProductRuleDTO> linearProductRules,
                                 List<SingleAssetOptionRuleDTO> singleAssetOptionRules,
                                 String description) {
        this.pricingEnvironmentId = pricingEnvironmentId;
        this.cashRule = cashRule;
        this.linearProductRules = linearProductRules;
        this.singleAssetOptionRules = singleAssetOptionRules;
        this.description = description;
    }

    public String getPricingEnvironmentId() {
        return pricingEnvironmentId;
    }

    public void setPricingEnvironmentId(String pricingEnvironmentId) {
        this.pricingEnvironmentId = pricingEnvironmentId;
    }

    public CashRuleDTO getCashRule() {
        return cashRule;
    }

    public void setCashRule(CashRuleDTO cashRule) {
        this.cashRule = cashRule;
    }

    public List<LinearProductRuleDTO> getLinearProductRules() {
        return linearProductRules;
    }

    public void setLinearProductRules(List<LinearProductRuleDTO> linearProductRules) {
        this.linearProductRules = linearProductRules;
    }

    public List<SingleAssetOptionRuleDTO> getSingleAssetOptionRules() {
        return singleAssetOptionRules;
    }

    public void setSingleAssetOptionRules(List<SingleAssetOptionRuleDTO> singleAssetOptionRules) {
        this.singleAssetOptionRules = singleAssetOptionRules;
    }

    public String getDescription() {
        return description;
    }
}

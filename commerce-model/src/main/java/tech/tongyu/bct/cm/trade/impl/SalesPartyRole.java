package tech.tongyu.bct.cm.trade.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRole;
import tech.tongyu.bct.cm.trade.NonEconomicPartyRoleType;

import java.math.BigDecimal;

public class SalesPartyRole implements NonEconomicPartyRole {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Party branch;

    public String salesName;

    public BigDecimal salesCommission;

    public BigDecimal salesCommissionPercentage;

    public BigDecimal salesQuotation;

    public SalesPartyRole() {
    }

    public SalesPartyRole(Party branch, String salesName, BigDecimal salesCommission,
                          BigDecimal salesCommissionPercentage, BigDecimal salesQuotation) {
        this.branch = branch;
        this.salesName = salesName;
        this.salesCommission = salesCommission;
        this.salesCommissionPercentage = salesCommissionPercentage;
        this.salesQuotation = salesQuotation;
    }

    public SalesPartyRole(Party branch, String salesName, Double salesCommission,
                          Double salesCommissionPercentage, Double salesQuotation) {
        this.branch = branch;
        this.salesName = salesName;
        this.salesCommission = BigDecimal.valueOf(salesCommission);
        this.salesCommissionPercentage = BigDecimal.valueOf(salesCommissionPercentage);
        this.salesQuotation = BigDecimal.valueOf(salesQuotation);
    }

    @Override
    public Party party() {
        return branch;
    }

    @Override
    public NonEconomicPartyRoleType roleType() {
        return NonEconomicPartyRoleType.Salesperson;
    }
}

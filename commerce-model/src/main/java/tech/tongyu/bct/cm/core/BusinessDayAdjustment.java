package tech.tongyu.bct.cm.core;

import java.util.List;

public class BusinessDayAdjustment {
    public BusinessDayConventionTypeEnum businessDayConvention;

    public List<BusinessCenterEnum> businessCenters;

    public BusinessDayAdjustment() {
    }

    public BusinessDayAdjustment(BusinessDayConventionTypeEnum businessDayConvention, List<BusinessCenterEnum> businessCenters) {
        this.businessDayConvention = businessDayConvention;
        this.businessCenters = businessCenters;
    }

    public BusinessDayConventionTypeEnum businessDayConvention() {
        return businessDayConvention;
    }

    public List<BusinessCenterEnum> businessCenters() {
        return businessCenters;
    }
}

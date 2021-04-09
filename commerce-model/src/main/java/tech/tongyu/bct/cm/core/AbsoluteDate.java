package tech.tongyu.bct.cm.core;

import java.time.LocalDate;
import java.util.Arrays;

public class AbsoluteDate implements AdjustedDate {

    public LocalDate unadjustedDate;

    public BusinessDayAdjustment adjustment;

    public AbsoluteDate() {
    }

    public AbsoluteDate(LocalDate unadjustedDate, BusinessDayAdjustment adjustment) {
        this.unadjustedDate = unadjustedDate;
        this.adjustment = adjustment;
    }

    public AbsoluteDate(LocalDate unadjustedDate) {
        this.unadjustedDate = unadjustedDate;
        this.adjustment = new BusinessDayAdjustment(
                BusinessDayConventionTypeEnum.NONE, Arrays.asList());
    }

    public LocalDate unadjustedDate() {
        return unadjustedDate;
    }

    public BusinessDayAdjustment adjustment() {
        return adjustment;
    }
}

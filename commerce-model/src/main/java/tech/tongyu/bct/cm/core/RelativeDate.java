package tech.tongyu.bct.cm.core;

public class RelativeDate implements AdjustedDate {
    public Offset offset;

    public BusinessDayAdjustment adjustment;

    public RelativeDate() {
    }

    public RelativeDate(Offset offset, BusinessDayAdjustment adjustment) {
        this.offset = offset;
        this.adjustment = adjustment;
    }

    public Offset offset() {
        return offset;
    }

    @Override
    public BusinessDayAdjustment adjustment() {
        return adjustment;
    }
}

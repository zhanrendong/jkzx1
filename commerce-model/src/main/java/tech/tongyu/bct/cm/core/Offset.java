package tech.tongyu.bct.cm.core;

public class Offset {
    public Integer periodMultiplier;

    public PeriodTypeEnum period;

    public DayTypeEnum dayType;

    public Offset() {
    }

    public Offset(Integer periodMultiplier, PeriodTypeEnum period, DayTypeEnum dayType) {
        this.periodMultiplier = periodMultiplier;
        this.period = period;
        this.dayType = dayType;
    }

    public Integer periodMultiplier() {
        return periodMultiplier;
    }

    PeriodTypeEnum period() {
        return period;
    }

    DayTypeEnum dayType() {
        return dayType;
    }
}

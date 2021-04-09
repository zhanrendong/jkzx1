package tech.tongyu.bct.quant.library.common.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.common.QuantPricerParams;

import java.util.Arrays;
import java.util.List;

public class BlackAnalyticPricerParams implements QuantPricerParams {
    private final List<String> calendars;
    private final boolean useCalendar;
    private final double daysInYear;

    @JsonCreator
    public BlackAnalyticPricerParams(boolean useCalendar, List<String> calendars, double daysInYear) {
        this.calendars = calendars;
        this.useCalendar = useCalendar;
        this.daysInYear = daysInYear;
    }

    public BlackAnalyticPricerParams() {
        this.calendars = Arrays.asList("DEFAULT_CALENDAR");
        this.useCalendar = false;
        this.daysInYear = 365;
    }

    public List<String> getCalendars() {
        return calendars;
    }

    public boolean isUseCalendar() {
        return useCalendar;
    }

    public double getDaysInYear() {
        return daysInYear;
    }
}

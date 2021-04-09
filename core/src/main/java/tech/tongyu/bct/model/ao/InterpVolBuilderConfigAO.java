package tech.tongyu.bct.model.ao;

import tech.tongyu.bct.market.dto.InstanceEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 行权价上插值的波动率曲面波动率数据(vol grid)
 */
public class InterpVolBuilderConfigAO {
    /**
     * 模型名称。同一名称可以按估值日，创建时间有不同版本/实例。
     */
    private String modelName;
    /**
     * 估值日, 即波动率曲面起始日期
     */
    private LocalDate valuationDate;
    /**
     * 时区
     */
    private ZoneId timezone;
    /**
     * 日内/收盘
     */
    private InstanceEnum instance;
    /**
     * 标的物{@link VolUnderlyerAO}
     */
    private VolUnderlyerAO underlyer;
    /**
     * 每个到期日/期限不同行权价上的波动率 {@link InterpVolInstrumentRowAO}
     */
    private List<InterpVolInstrumentRowAO> volGrid;

    /**
     * 一年的天数
     */
    private double daysInYear;

    /**
     * 使用交易日历计算期限
     */
    private boolean useCalendarForTau;

    /**
     * 计算期限使用的交易日历
     */
    private List<String> calendars;

    /**
     * 使用波动率日历对波动率插值
     */
    private boolean useVolCalendar;

    /**
     * 波动率插值使用的波动率日历
     */
    private String volCalendar;

    public InterpVolBuilderConfigAO() {
    }

    public InterpVolBuilderConfigAO(String modelName, LocalDate valuationDate, ZoneId timezone,
                                    InstanceEnum instance, VolUnderlyerAO underlyer,
                                    List<InterpVolInstrumentRowAO> volGrid, double daysInYear) {
        this.modelName = modelName;
        this.valuationDate = valuationDate;
        this.timezone = timezone;
        this.instance = instance;
        this.underlyer = underlyer;
        this.volGrid = volGrid;
        this.daysInYear = daysInYear;
        this.useCalendarForTau = false;
        this.useVolCalendar = false;
    }

    public InterpVolBuilderConfigAO(String modelName, LocalDate valuationDate, ZoneId timezone,
                                    InstanceEnum instance, VolUnderlyerAO underlyer,
                                    List<InterpVolInstrumentRowAO> volGrid,
                                    double daysInYear, boolean useCalendarForTau, List<String> calendars,
                                    boolean useVolCalendar, String volCalendar) {
        this.modelName = modelName;
        this.valuationDate = valuationDate;
        this.timezone = timezone;
        this.instance = instance;
        this.underlyer = underlyer;
        this.volGrid = volGrid;
        this.daysInYear = daysInYear;
        this.useCalendarForTau = useCalendarForTau;
        this.calendars = calendars;
        this.useVolCalendar = useVolCalendar;
        this.volCalendar = volCalendar;
    }

    public String getModelId() {
        return modelName + "|VOL_SURFACE|" + instance + "|" + underlyer.getInstrumentId() + "|"
                + valuationDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "|"
                + timezone;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public VolUnderlyerAO getUnderlyer() {
        return underlyer;
    }

    public void setUnderlyer(VolUnderlyerAO underlyer) {
        this.underlyer = underlyer;
    }

    public List<InterpVolInstrumentRowAO> getVolGrid() {
        return volGrid;
    }

    public void setVolGrid(List<InterpVolInstrumentRowAO> volGrid) {
        this.volGrid = volGrid;
    }

    public double getDaysInYear() {
        return daysInYear;
    }

    public void setDaysInYear(double daysInYear) {
        this.daysInYear = daysInYear;
    }

    public boolean isUseCalendarForTau() {
        return useCalendarForTau;
    }

    public void setUseCalendarForTau(boolean useCalendarForTau) {
        this.useCalendarForTau = useCalendarForTau;
    }

    public List<String> getCalendars() {
        return calendars;
    }

    public void setCalendars(List<String> calendars) {
        this.calendars = calendars;
    }

    public boolean isUseVolCalendar() {
        return useVolCalendar;
    }

    public void setUseVolCalendar(boolean useVolCalendar) {
        this.useVolCalendar = useVolCalendar;
    }

    public String getVolCalendar() {
        return volCalendar;
    }

    public void setVolCalendar(String volCalendar) {
        this.volCalendar = volCalendar;
    }
}

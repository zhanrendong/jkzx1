package tech.tongyu.bct.user.preference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.List;

public class PreferenceDTO {
    @BctField(name = "userName", description = "用户名", type = "String")
    String userName;
    @BctField(name = "volSurfaceInstrumentIds", description = "波动率标的物列表", type = "List<String>")
    List<String> volSurfaceInstrumentIds;
    @BctField(name = "dividendCurveInstrumentIds", description = "分红曲线标的物列表", type = "List<String>")
    List<String> dividendCurveInstrumentIds;

    public PreferenceDTO(String userName, List<String> volSurfaceInstrumentIds, List<String> dividendCurveInstrumentIds) {
        this.userName = userName;
        this.volSurfaceInstrumentIds = volSurfaceInstrumentIds;
        this.dividendCurveInstrumentIds = dividendCurveInstrumentIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getVolSurfaceInstrumentIds() {
        return volSurfaceInstrumentIds;
    }

    public void setVolSurfaceInstrumentIds(List<String> volSurfaceInstrumentIds) {
        this.volSurfaceInstrumentIds = volSurfaceInstrumentIds;
    }

    public List<String> getDividendCurveInstrumentIds() {
        return dividendCurveInstrumentIds;
    }

    public void setDividendCurveInstrumentIds(List<String> dividendCurveInstrumentIds) {
        this.dividendCurveInstrumentIds = dividendCurveInstrumentIds;
    }
}

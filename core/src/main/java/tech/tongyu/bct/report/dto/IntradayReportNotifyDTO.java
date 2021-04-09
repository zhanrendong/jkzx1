package tech.tongyu.bct.report.dto;

import java.time.LocalDateTime;

public class IntradayReportNotifyDTO {

    private IntradayReportTypeEnum reportType;

    private LocalDateTime valuationTime;

    public IntradayReportTypeEnum getReportType() {
        return reportType;
    }

    public void setReportType(IntradayReportTypeEnum reportType) {
        this.reportType = reportType;
    }

    public LocalDateTime getValuationTime() {
        return valuationTime;
    }

    public void setValuationTime(LocalDateTime valuationTime) {
        this.valuationTime = valuationTime;
    }
}

package tech.tongyu.bct.report.api.client;


import tech.tongyu.bct.common.api.doc.BctField;

public enum ReportSearchableFieldEnum {
    @BctField(description = "创建日期")
    CREATED_AT("createdAt"),
    @BctField(description = "交易日")
    TRADE_DATE("tradeDate"),
    @BctField(description = "到期日")
    EXPIRATION_DATE("expirationDate"),
    @BctField(description = "交易簿名称")
    BOOK_NAME("bookName"),
    @BctField(description = "标的物代码")
    INSTRUMENT_ID("underlyerInstrumentId"),
    @BctField(description = "交易开始日期")
    DEAL_START_DATE("dealStartDate"),
    @BctField(description = "到期")
    EXPIRY("expiry")
;

    private String fieldName;

    ReportSearchableFieldEnum(String feildName) {
        this.fieldName = feildName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static ReportSearchableFieldEnum of(String feildName) {
        for (ReportSearchableFieldEnum item : values()) {
            if (item.getFieldName().equals(feildName)) {
                return item;
            }
        }
        return null;
    }
}

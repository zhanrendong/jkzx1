package tech.tongyu.bct.document.common;

public interface Constant {

    String SUCCESS = "SUCCESS";
    String ERROR = "ERROR";

    String REGEX_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    String UTF_8 = "UTF-8";
    String CSV_POSITION = "position.csv";
    String XLSX_MARGIN = "margin.xlsx";
    String DOCX_POI = "poi.docx";

    String COMMON_CONTENT_DISPOSITION = "attachment;filename*=UTF-8''";
    String EXCHANGE_FLOW_TEMPLATE = "导入场内流水模板.csv";
    String BATCH_UPDATE_MARGIN_TEMPLATE = "批量更新维持保证金模板.xls";
    String REPLACE_TRADE_DOC = "交易文档替换字段.docx";

    String CONTENT_DISPOSITION = "Content-Disposition";

    String STATUS_TEXT = "statusText";
}

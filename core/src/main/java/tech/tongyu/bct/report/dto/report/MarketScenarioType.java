package tech.tongyu.bct.report.dto.report;

public enum MarketScenarioType {
    STOCK_CRASH_2015("股市异常波动（2015/6/26）"),
    TRADE_WAR_2018("贸易战（2018/3/22）"),
    FINANCIAL_CRISIS_2008("2008年金融危机（2008/9/16）"),
    BASE("今日市场风险");

    private String description;

    MarketScenarioType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

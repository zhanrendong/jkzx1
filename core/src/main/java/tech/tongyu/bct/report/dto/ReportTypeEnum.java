package tech.tongyu.bct.report.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ReportTypeEnum {
    /**
     * 存续期交易持仓详情
     */
    @BctField(description = "存续期交易持仓详情")
    LIVE_POSITION_INFO,
    /**
     * 历史盈亏
     */
    @BctField(description = "历史盈亏")
    PNL_HST,
    /**
     * EOD Mark-to-Market PV and Risks
     */
    @BctField(description = "EOD Mark-to-Market PV and Risks")
    RISK,

    /**
     * EOD PnL
     */
    @BctField(description = "EOD PnL")
    PNL,
    /**
     *
     * 场外期权业务交易报表
     */
    @BctField(description = "场外期权业务交易报表")
    FOF,
    /**
     * 场外期权业务资金明细报表
     */
    @BctField(description = "场外期权业务资金明细报表")
    FOT,
    /**
     * 场外期权业务客户资金汇总报表
     */
    @BctField(description = "场外期权业务客户资金汇总报表")
    FOC,
    /**
     *对冲端盈亏表
     */
    @BctField(description = "对冲端盈亏表")
    IHP,
    /**
     *利润统计表
     */
    @BctField(description = "利润统计表")
    IPS,
    /**
     *风险指标统计表
     */
    @BctField(description = "风险指标统计表")
    ISO,
    /**
     * 资产统计表
     */
    @BctField(description = "资产统计表")
    ISC,
    /**
     * 监管报告
     */
    @BctField(description = "监管报告")
    REG,
    /**
     * 自定义
     */
    @BctField(description = "自定义")
    CUSTOM,
    /**
     * 全市场整体风险汇总报告
     */
    MARKET_RISK,
    /**
     * 全市场分品种风险汇总报告
     */
    MARKET_RISK_DETAIL,
    /**
     * 子公司整体风险汇总报告
     */
    SUBSIDIARY_RISK,
    /**
     * 子公司分品种风险汇总报告
     */
    SUBSIDIARY_RISK_DETAIL,
    /**
     * 交易对手整体风险汇总报告
     */
    PARTY_RISK,
    /**
     * 交易对手分品种风险汇总报告
     */
    PARTY_RISK_DETAIL;

    public static boolean isExist(String reportType){
        return Arrays.stream(ReportTypeEnum.values())
                .map(v-> v.name())
                .collect(Collectors.toList())
                .contains(reportType);
    }
}

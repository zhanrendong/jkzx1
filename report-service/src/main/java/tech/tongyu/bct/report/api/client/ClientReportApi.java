package tech.tongyu.bct.report.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.document.dto.TemplateDTO;
import tech.tongyu.bct.document.dto.TemplateDirectoryDTO;
import tech.tongyu.bct.document.ext.dto.BctTemplateDTO;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.ext.service.BctDocumentService;
import tech.tongyu.bct.document.service.DocumentService;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.PartyService;
import tech.tongyu.bct.report.ReportConstants;
import tech.tongyu.bct.report.client.dto.*;
import tech.tongyu.bct.report.client.service.ClientReportInputService;
import tech.tongyu.bct.report.client.service.ClientReportService;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.io.StringWriter;
import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientReportApi {

    private ClientReportService clientReportService;
    private ClientReportInputService clientReportInputService;
    private BctDocumentService bctDocumentService;
    private DocumentService documentService;
    private PartyService partyService;
    private ReportHelper reportHelper;

    @Autowired
    public ClientReportApi(ClientReportService clientReportService,
                           ClientReportInputService clientReportInputService,
                           BctDocumentService bctDocumentService,
                           ResourceAuthAction resourceAuthAction,
                           DocumentService documentService,
                           PartyService partyService) {
        this.clientReportService = clientReportService;
        this.clientReportInputService = clientReportInputService;
        this.bctDocumentService = bctDocumentService;
        this.documentService = documentService;
        this.partyService = partyService;
        this.reportHelper = new ReportHelper(resourceAuthAction);
    }

    @BctMethodInfo(
            description = "获取所有指定报告种类的报告名",
            retDescription = "所有指定报告种类的报告名",
            retName = "List of report names",
            service = "report-service"
    )
    public List<String> rptReportNameList(
            @BctMethodArg(description = "报告类型", argClass = ReportTypeEnum.class) String reportType
    ) {
        reportHelper.paramCheck(reportType, "请输入报告类型");
        ReportTypeEnum reportTypeEnum = ReportTypeEnum.valueOf(reportType);
        return clientReportService.findReportNamesByType(reportTypeEnum);

    }

    @BctMethodInfo(
            description = "录入日终汇总日盈亏报告",
            service = "report-service"
    )
    public void rptPnlReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "日终汇总日盈亏报告", argClass = PnlReportDTO.class) List<Map<String, Object>> pnlReports) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(pnlReports, "盈亏归因数据不存在");
        clientReportService.deletePnlReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createPnlReport(pnlReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入日终汇总风险报告",
            service = "report-service"
    )
    public void rptRiskReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "日终汇总风险报告", argClass = RiskReportDTO.class) List<Map<String, Object>> riskReports
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(riskReports, "风险报告数据不存在");
        clientReportService.deleteRiskReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createRiskReport(riskReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入日终历史盈亏报告",
            service = "report-service"
    )
    public void rptPnlHstReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "日终历史盈亏报告", argClass = PnlHstReportDTO.class) List<Map<String, Object>> pnlHstReports
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(pnlHstReports, "历史盈亏数据不存在");
        clientReportService.deletePnlHstReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createPnlHstReport(pnlHstReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入日终持仓明细报告",
            service = "report-service"
    )
    public void rptPositionReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "日终持仓明细报告", argClass = PositionReportDTO.class) List<Map<String, Object>> positionReports
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(positionReports, "交易详情数据不存在");
        clientReportService.deletePositionReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createPositionReport(positionReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入交易报表",
            service = "report-service"
    )
    public void rptOtcTradeReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "交易报表", argClass = FinancialOtcTradeReportDTO.class) List<Map<String, Object>> otcTradeReports
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(otcTradeReports, "场外期权业务交易数据不存在");
        String msg = new StringBuilder()
                .append("delete FinancialOtcTradeReport of ")
                .append(reportName).append(" on ").append(valuationDate).toString();
        ProfilingUtils.timed(msg, () -> {
            clientReportService.deleteOtcTradeReportByReportNameAndValuationDate(reportName, valuationDate);
            return null;
        });
        msg = new StringBuilder()
                .append("create ")
                .append(otcTradeReports.size())
                .append(" FinancialOtcTradeReport of ")
                .append(reportName).append(" on ").append(valuationDate).toString();
        ProfilingUtils.timed(msg, () -> {
            clientReportInputService.createOtcTradeReport(otcTradeReports, reportName, valuationDate);
            return null;
        });
    }

    @BctMethodInfo(
            description = "录入资金明细报表",
            service = "report-service"
    )
    public void rptFinancialOtcFundDetailReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "资金明细报表", argClass = FinancialOtcFundDetailReportDTO.class) List<Map<String, Object>> financialOtcFundDetailReports
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(financialOtcFundDetailReports, "场外期权业务资金明细报数据不存在");
        clientReportService.deleteFinancialOtcFundDetailReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createFinancialOtcFundDetailReport(financialOtcFundDetailReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入客户资金汇总报表",
            service = "report-service"
    )
    public void rptFinanicalOtcClientFundReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "客户资金汇总报表", argClass = FinanicalOtcClientFundReportDTO.class) List<Map<String, Object>> finanicalOtcClientFundReports
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(finanicalOtcClientFundReports, "场外期权业务客户资金汇总数据不存在");
        clientReportService.deleteFinanicalOtcClientFundReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createFinanicalOtcClientFundReport(finanicalOtcClientFundReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入对冲端盈亏表",
            service = "report-service"
    )
    public void rptEodHedgePnlReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "对冲端盈亏表", argClass = HedgePnlReportDTO.class) List<Map<String, Object>> eodHedgePnlReports) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(eodHedgePnlReports, "对冲端盈亏数据不存在");
        clientReportService.deleteEodHedgePnlReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createHedgePnlReport(eodHedgePnlReports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入利润统计表",
            service = "report-service"
    )
    public void rptEodProfitStatisicsReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "利润统计表", argClass = ProfitStatisicsReportDTO.class) List<Map<String, Object>> eodProfitStatisicsReport) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(eodProfitStatisicsReport, "利润统计数据不存在");
        clientReportService.deleteEodProfitStatisicsReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createProfitStatisicsReport(eodProfitStatisicsReport, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入风险指标统计表",
            service = "report-service"
    )
    public void rptEodStatisticsOfRiskReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "风险指标统计表", argClass = StatisticsOfRiskReportDTO.class) List<Map<String, Object>> eodStatisticsOfRiskReport
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(eodStatisticsOfRiskReport, "风险指标统计数据不存在");
        clientReportService.deleteEodStatisticsOfRiskReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createStatisticsOfRiskReport(eodStatisticsOfRiskReport, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "录入资产统计表",
            service = "report-service"
    )
    public void rptEodSubjectComputingReportCreateBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "资产统计表", argClass = SubjectComputingReportDTO.class) List<Map<String, Object>> eodSubjectComputingReport) {

        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(eodSubjectComputingReport, "资产统计数据不存在");
        clientReportService.deleteEodSubjectComputingReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createSubjectComputingReport(eodSubjectComputingReport, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "全市场整体风险汇总报告",
            service = "report-service"
    )
    @Transactional
    public void rptMarketRiskReportCreateBatch(@BctMethodArg String reportName,
                                               @BctMethodArg String valuationDate,
                                               @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "全市场整体风险汇总报告数据不存在");
        clientReportService.deleteMarketRiskReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createMarketRiskReport(reports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "全市场分品种风险报告",
            service = "report-service"
    )
    @Transactional
    public void rptMarketRiskDetailReportCreateBatch(@BctMethodArg(required = false) String reportName,
                                                     @BctMethodArg String valuationDate,
                                                     @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "报告数据不存在");
        clientReportService.deleteMarketRiskDetailReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createMarketRiskDetailReport(reports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "各子公司整体风险报告",
            service = "report-service"
    )
    @Transactional
    public void rptSubsidiaryMarketRiskReportCreateBatch(@BctMethodArg(required = false) String reportName,
                                                         @BctMethodArg String valuationDate,
                                                         @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "各子公司整体风险报告数据不存在");
        clientReportService.deleteSubsidiaryMarketRiskReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createSubsidiaryMarketRiskReport(reports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "交易对手风险报告",
            service = "report-service"
    )
    @Transactional
    public void rptCounterPartyMarketRiskReportCreateBatch(@BctMethodArg(required = false) String reportName,
                                                           @BctMethodArg String valuationDate,
                                                           @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "交易对手风险报告数据不存在");
        clientReportService.deleteCounterPartyMarketRiskReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createCounterPartyMarketRiskReport(reports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "交易对手分品种风险报告",
            service = "report-service"
    )
    @Transactional
    public void rptCounterPartyMarketRiskByUnderlyerReportCreateBatch(@BctMethodArg(required = false) String reportName,
                                                                      @BctMethodArg String valuationDate,
                                                                      @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "交易对手分品种风险报告数据不存在");
        clientReportService.deleteCounterPartyMarketRiskByUnderlyerReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createCounterPartyMarketRiskByUnderlyerReport(reports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "各子公司分品种风险",
            service = "report-service"
    )
    @Transactional
    public void rptMarketRiskBySubUnderlyerReportCreateBatch(@BctMethodArg String reportName,
                                                             @BctMethodArg String valuationDate,
                                                             @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "各子公司分品种风险报告数据不存在");
        clientReportService.deleteMarketRiskBySubUnderlyerReportByReportNameAndValuationDate(reportName, valuationDate);
        clientReportInputService.createMarketRiskBySubUnderlyerReport(reports, reportName, valuationDate);
    }

    @BctMethodInfo(
            description = "情景分析报告",
            service = "report-service"
    )
    @Transactional
    public void rptSpotScenariosReportCreateBatch(@BctMethodArg String valuationDate,
                                                  @BctMethodArg List<Map<String, Object>> reports) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reports, "情景分析报告数据不存在");
        clientReportInputService.createSpotScenariosReport(reports, valuationDate);
    }

    @BctMethodInfo(
            description = "按日期删除情景分析报告",
            service = "report-service"
    )
    @Transactional
    public void rptDeleteSpotScenariosReportByDate(@BctMethodArg String valuationDate) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        clientReportService.deleteSpotScenariosReportByValuationDate(valuationDate);
    }

    @BctMethodInfo(
            description = "查看情景分析报告",
            retDescription = "情景分析报告",
            retName = "List<SpotScenariosReportDTO>",
            returnClass = SpotScenariosReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public List<SpotScenariosReportDTO> rptSpotScenariosReportListSearch(@BctMethodArg String reportType,
                                                                         @BctMethodArg String valuationDate,
                                                                         @BctMethodArg(required = false) String subOrPartyName,
                                                                         @BctMethodArg String underlyer) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(underlyer, "请选择待查询标的物");
        return clientReportService.findSpotScenariosReportByDateAndTypeAndInstrument(DataRangeEnum.valueOf(reportType), subOrPartyName, LocalDate.parse(valuationDate), underlyer);
    }


    @BctMethodInfo(
            description = "按日期获取日终汇总日盈亏报告",
            retDescription = "日终汇总日盈亏报告",
            retName = "List of PnlReportDTO",
            returnClass = PnlReportDTO.class,
            service = "report-service"
    )
    public List<PnlReportDTO> rptPnlReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return reportHelper.filterReport(() -> clientReportService.findPnlReportByValuationDate(LocalDate.parse(valuationDate)));
    }

    @BctMethodInfo(
            description = "按日期和报告名获取日终汇总日盈亏报告",
            retDescription = "日终汇总日盈亏报告",
            retName = "List of PnlReportDTO",
            returnClass = PnlReportDTO.class,
            tags = {BctApiTagEnum.Excel},
            excelType = BctExcelTypeEnum.Table,
            service = "report-service"
    )
    public List<PnlReportDTO> rptPnlReportListByDateAndName(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return reportHelper.filterReport(() -> clientReportService.findPnlReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName));
    }

    @BctMethodInfo(
            description = "按日期获取日终汇总风险报告",
            retDescription = "日终汇总风险报告",
            retName = "List of RiskReportDTO",
            returnClass = RiskReportDTO.class,
            service = "report-service"
    )
    public List<RiskReportDTO> rptRiskReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return reportHelper.filterReport(() -> clientReportService.findRiskReportByValuationDate(LocalDate.parse(valuationDate)));
    }

    @BctMethodInfo(
            description = "按日期和报告名获取日终汇总风险报告",
            retDescription = "日终汇总风险报告",
            retName = "List of RiskReportDTO",
            returnClass = RiskReportDTO.class,
            service = "report-service"
    )
    public List<RiskReportDTO> rptRiskReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return reportHelper.filterReport(() -> clientReportService.findRiskReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName));
    }

    @BctMethodInfo(
            description = "按日期获取日终历史盈亏报告",
            retDescription = "日终历史盈亏报告",
            retName = "List of PnlHstReportDTO",
            returnClass = PnlHstReportDTO.class,
            service = "report-service"
    )
    public List<PnlHstReportDTO> rptPnlHstReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return reportHelper.filterReport(() -> clientReportService.findPnlHstReportByValuationDate(LocalDate.parse(valuationDate)));
    }

    @BctMethodInfo(
            description = "按日期和报告名获取日终历史盈亏报告",
            retDescription = "日终历史盈亏报告",
            retName = "List of PnlHstReportDTO",
            returnClass = PnlHstReportDTO.class,
            tags = {BctApiTagEnum.Excel},
            excelType = BctExcelTypeEnum.Table,
            service = "report-service"
    )
    public List<PnlHstReportDTO> rptPnlHstReportListByDateAndName(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime, description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return reportHelper.filterReport(() -> clientReportService.findPnlHstReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName));
    }

    @BctMethodInfo(
            description = "按日期获取日终持仓明细报告",
            retDescription = "日终持仓明细报告",
            retName = "List of PositionReportDTO",
            returnClass = PositionReportDTO.class,
            service = "report-service"
    )
    public List<PositionReportDTO> rptPositionReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return reportHelper.filterReport(() -> clientReportService.findPositionReportByValuationDate(LocalDate.parse(valuationDate)));
    }

    @BctMethodInfo(
            description = "按日期和报告名获取日终持仓明细报告",
            retDescription = "日终持仓明细报告",
            retName = "List of PositionReportDTO",
            returnClass = PositionReportDTO.class,
            service = "report-service"
    )
    public List<PositionReportDTO> rptPositionReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return reportHelper.filterReport(() -> clientReportService.findPositionReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName));
    }

    @BctMethodInfo(
            description = "按日期获取交易报表",
            retDescription = "交易报表",
            retName = "List of FinancialOtcTradeReportDTO",
            returnClass = FinancialOtcTradeReportDTO.class,
            service = "report-service"
    )
    public List<FinancialOtcTradeReportDTO> rptOtcTradeReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return reportHelper.filterReport(() -> clientReportService.findOtcTradeReportByValuationDate(LocalDate.parse(valuationDate)));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取交易报表",
            retDescription = "交易报表",
            retName = "List of FinancialOtcTradeReportDTO",
            returnClass = FinancialOtcTradeReportDTO.class,
            service = "report-service"
    )
    public List<FinancialOtcTradeReportDTO> rptOtcTradeReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return reportHelper.filterReport(() -> clientReportService.findOtcTradeReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName));
    }

    @BctMethodInfo(
            description = "按日期获取资金明细报表",
            retDescription = "资金明细报表",
            retName = "List of FinancialOtcFundDetailReportDTO",
            returnClass = FinancialOtcFundDetailReportDTO.class,
            service = "report-service"
    )
    public List<FinancialOtcFundDetailReportDTO> rptFinancialOtcFundDetailReportByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findFinancialOtcFundDetailReportByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取资金明细报表",
            retDescription = "资金明细报表",
            retName = "List of FinancialOtcFundDetailReportDTO",
            returnClass = FinancialOtcFundDetailReportDTO.class,
            service = "report-service"
    )
    public List<FinancialOtcFundDetailReportDTO> rptFinancialOtcFundDetailReportByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findFinancialOtcFundDetailReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "按日期获取客户资金汇总报表",
            retDescription = "客户资金汇总报表",
            retName = "List of FinanicalOtcClientFundReportDTO",
            returnClass = FinanicalOtcClientFundReportDTO.class,
            service = "report-service"
    )
    public List<FinanicalOtcClientFundReportDTO> rptFinanicalOtcClientFundReportByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findFinanicalOtcClientFundReportByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取客户资金汇总报表",
            retDescription = "客户资金汇总报表",
            retName = "List of FinanicalOtcClientFundReportDTO",
            returnClass = FinanicalOtcClientFundReportDTO.class,
            service = "report-service"
    )
    public List<FinanicalOtcClientFundReportDTO> rptFinanicalOtcClientFundReportByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findFinanicalOtcClientFundReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "按日期获取对冲端盈亏表",
            retDescription = "对冲端盈亏表",
            retName = "List of HedgePnlReportDTO",
            returnClass = HedgePnlReportDTO.class,
            service = "report-service"
    )
    public List<HedgePnlReportDTO> rptIntradayHedgePnlReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findAllHedgePnlReportByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取对冲端盈亏表",
            retDescription = "对冲端盈亏表",
            retName = "List of HedgePnlReportDTO",
            returnClass = HedgePnlReportDTO.class,
            service = "report-service"
    )
    public List<HedgePnlReportDTO> rptIntradayHedgePnlReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findAllHedgePnlReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "按日期获取利润统计表",
            retDescription = "利润统计表",
            retName = "List of ProfitStatisicsReportDTO",
            returnClass = ProfitStatisicsReportDTO.class,
            service = "report-service"
    )
    public List<ProfitStatisicsReportDTO> rptIntradayProfitStatisicsReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findAllProfitStatisicsReportByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取利润统计表",
            retDescription = "利润统计表",
            retName = "List of ProfitStatisicsReportDTO",
            returnClass = ProfitStatisicsReportDTO.class,
            service = "report-service"
    )
    public List<ProfitStatisicsReportDTO> rptIntradayProfitStatisicsReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findAllProfitStatisicsReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "按日期获取风险指标统计表",
            retDescription = "风险指标统计表",
            retName = "List of StatisticsOfRiskReportDTO",
            returnClass = StatisticsOfRiskReportDTO.class,
            service = "report-service"
    )
    public List<StatisticsOfRiskReportDTO> rptIntradayStatisticsOfRiskReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findAllStatisticsOfRiskReportByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取风险指标统计表",
            retDescription = "风险指标统计表",
            retName = "List of StatisticsOfRiskReportDTO",
            returnClass = StatisticsOfRiskReportDTO.class,
            service = "report-service"
    )
    public List<StatisticsOfRiskReportDTO> rptIntradayStatisticsOfRiskReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findAllStatisticsOfRiskReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "按日期获取资产统计表",
            retDescription = "资产统计表",
            retName = "List of SubjectComputingReportDTO",
            returnClass = SubjectComputingReportDTO.class,
            service = "report-service"
    )
    public List<SubjectComputingReportDTO> rptIntradaySubjectComputingReportListByDate(
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findAllSubjectComputingReportByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按日期和报表名获取资产统计表",
            retDescription = "资产统计表",
            retName = "List of SubjectComputingReportDTO",
            returnClass = SubjectComputingReportDTO.class,
            service = "report-service"
    )
    public List<SubjectComputingReportDTO> rptIntradaySubjectComputingReportListByDateAndName(
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "报告名称") String reportName
    ) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findAllSubjectComputingReportByValuationDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "查看全市场整体风险报告",
            retDescription = "全市场整体风险报告",
            retName = "List<MarketRiskReportDTO>",
            returnClass = MarketRiskReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public List<MarketRiskReportDTO> rptMarketRiskReportListByDate(@BctMethodArg String valuationDate) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        return clientReportService.findAllMarketRiskReportByDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo
    public List<MarketRiskReportDTO> rptMarketRiskReportListByDateAndName(@BctMethodArg String valuationDate,
                                                                          @BctMethodArg String reportName) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(reportName, "请输入待查询报告名称");
        return clientReportService.findAllMarketRiskReportByDateAndName(LocalDate.parse(valuationDate), reportName);
    }

    @BctMethodInfo(
            description = "查看全市场分品种风险报告",
            retDescription = "全市场分品种风险报告",
            retName = "RpcResponseListPaged<MarketRiskDetailReportDTO>",
            returnClass = MarketRiskDetailReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public RpcResponseListPaged<MarketRiskDetailReportDTO> rptSearchPagedMarketRiskDetailReport(
            @BctMethodArg String valuationDate,
            @BctMethodArg(required = false) Integer page,
            @BctMethodArg(required = false) Integer pageSize,
            @BctMethodArg(required = false) String instrumentIdPart,
            @BctMethodArg(required = false) String order,
            @BctMethodArg(required = false) String orderBy) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        if (!Objects.isNull(page) || !Objects.isNull(pageSize)) {
            if (Objects.isNull(page) || Objects.isNull(pageSize))
                throw new IllegalArgumentException("当前页数和页面数量要同时不为空或同时为空");
        }
        final String orderedBy = StringUtils.isBlank(orderBy) ? "underlyerInstrumentId" : orderBy;
        List<MarketRiskDetailReportDTO> reports = clientReportService.findAllMarketDetailRiskReportByDateAndInstrument(
                LocalDate.parse(valuationDate), instrumentIdPart).stream().sorted((item1, item2) -> {
            int compareResult = 0;
            if (orderedBy.equals("delta")) {
                compareResult = item1.getDelta().compareTo(item2.getDelta());
            } else if (orderedBy.equals("deltaCash")) {
                compareResult = item1.getDeltaCash().compareTo(item2.getDeltaCash());
            } else if (orderedBy.equals("gamma")) {
                compareResult = item1.getGamma().compareTo(item2.getGamma());
            } else if (orderedBy.equals("gammaCash")) {
                compareResult = item1.getGammaCash().compareTo(item2.getGammaCash());
            } else if (orderedBy.equals("vega")) {
                compareResult = item1.getVega().compareTo(item2.getVega());
            } else if (orderedBy.equals("theta")) {
                compareResult = item1.getTheta().compareTo(item2.getTheta());
            } else if (orderedBy.equals("rho")) {
                compareResult = item1.getRho().compareTo(item2.getRho());
            } else {
                compareResult = item1.getUnderlyerInstrumentId().compareTo(item2.getUnderlyerInstrumentId());
            }
            if (compareResult == 0) {
                compareResult = item1.getCreatedAt().compareTo(item2.getCreatedAt());
            }
            return !StringUtils.isBlank(order) && order.equals("desc") ? -compareResult : compareResult;
        }).collect(Collectors.toList());
        page = Objects.isNull(page) ? 0 : page;
        pageSize = Objects.isNull(pageSize) ? Integer.MAX_VALUE : pageSize;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, reports.size());
        return new RpcResponseListPaged<>(reports.subList(start, end), reports.size());
    }

    @BctMethodInfo
    public List<MarketRiskDetailReportDTO> rptClassicScenarioMarketRiskReportListByDate(
            @BctMethodArg String valuationDate,
            @BctMethodArg String instrumentId,
            @BctMethodArg String reportType,
            @BctMethodArg(required = false) String subsidiary,
            @BctMethodArg(required = false) String partyName) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        reportHelper.paramCheck(instrumentId, "请输入待查询标的物");
        reportHelper.paramCheck(reportType, "请输入待查询报告类型");
        return clientReportService.findClassicScenarioMarketRiskReportByDateAndInstrument(
                LocalDate.parse(valuationDate), instrumentId, DataRangeEnum.valueOf(reportType), subsidiary, partyName);
    }

    @BctMethodInfo(
            description = "查看各子公司分品种风险报告",
            retDescription = "各子公司分品种风险报告",
            retName = "RpcResponseListPaged<MarketRiskBySubUnderlyerReportDTO>",
            returnClass = MarketRiskBySubUnderlyerReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public RpcResponseListPaged<MarketRiskBySubUnderlyerReportDTO> rptSearchPagedMarketRiskBySubUnderlyerReport(
            @BctMethodArg String valuationDate,
            @BctMethodArg(required = false) Integer page,
            @BctMethodArg(required = false) Integer pageSize,
            @BctMethodArg(required = false) String bookNamePart,
            @BctMethodArg(required = false) String instrumentIdPart,
            @BctMethodArg(required = false) String order,
            @BctMethodArg(required = false) String orderBy) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        if (!Objects.isNull(page) || !Objects.isNull(pageSize)) {
            if (Objects.isNull(page) || Objects.isNull(pageSize))
                throw new IllegalArgumentException("当前页数和页面数量要同时不为空或同时为空");
        }
        List<String> bookIds = reportHelper.getReadableBook(true);
        if (!StringUtils.isBlank(bookNamePart)) {
            bookIds = bookIds.stream().filter(v -> v.contains(bookNamePart)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bookIds)) {
                throw new CustomException("不存在子公司:" + bookNamePart);
            }
        }
        Comparator<MarketRiskBySubUnderlyerReportDTO> comparator = new Comparator<MarketRiskBySubUnderlyerReportDTO>() {
            @Override
            public int compare(MarketRiskBySubUnderlyerReportDTO o1, MarketRiskBySubUnderlyerReportDTO o2) {
                Collator collator = Collator.getInstance(Locale.CHINESE);
                if (collator.compare(o1.getBookName(), o2.getBookName()) > 0){
                    return 1;
                }else if (collator.compare(o1.getBookName(), o2.getBookName()) < 0){
                    return -1;
                }
                return o1.getUnderlyerInstrumentId().compareTo(o2.getUnderlyerInstrumentId());
            }
        };
        orderBy = StringUtils.isEmpty(orderBy) ? "default" : orderBy;
        switch (orderBy) {
            case "underlyerInstrumentId":
                comparator = new Comparator<MarketRiskBySubUnderlyerReportDTO>() {
                    @Override
                    public int compare(MarketRiskBySubUnderlyerReportDTO o1, MarketRiskBySubUnderlyerReportDTO o2) {
                        Collator collator = Collator.getInstance(Locale.CHINESE);
                        if (collator.compare(o1.getUnderlyerInstrumentId(), o2.getUnderlyerInstrumentId()) > 0){
                            return 1;
                        }else if (collator.compare(o1.getUnderlyerInstrumentId(), o2.getUnderlyerInstrumentId()) < 0){
                            return -1;
                        }
                        return o1.getBookName().compareTo(o2.getBookName());
                    }
                };
                break;
            case "delta":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getDelta)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "deltaCash":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getDeltaCash)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "gamma":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getGamma)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "gammaCash":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getGammaCash)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "vega":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getVega)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "theta":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getTheta)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "rho":
                comparator = Comparator.comparing(MarketRiskBySubUnderlyerReportDTO::getRho)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getBookName)
                        .thenComparing(MarketRiskBySubUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
        }

        List<MarketRiskBySubUnderlyerReportDTO> reports = clientReportService
                .findAllMarketRiskSubUnderlyerReportByDateAndInstrumentAndBook(LocalDate.parse(valuationDate), bookIds, instrumentIdPart);
        if ("default".equals(orderBy)){
            List<MarketRiskBySubUnderlyerReportDTO> cnList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getBookName().substring(0, 1).toUpperCase()) == -1)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            List<MarketRiskBySubUnderlyerReportDTO> enList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getBookName().substring(0, 1).toUpperCase()) != -1)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            cnList.addAll(enList);
            reports = cnList;
        }else {
            reports = reports.stream().sorted(comparator).collect(Collectors.toList());
        }

        if ("desc".equals(order)) {
            Collections.reverse(reports);
        }
        page = Objects.isNull(page) ? 0 : page;
        pageSize = Objects.isNull(pageSize) ? Integer.MAX_VALUE : pageSize;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, reports.size());
        return new RpcResponseListPaged<>(reports.subList(start, end), reports.size());
    }

    @BctMethodInfo(
            description = "查看交易对手分品种风险报告",
            retDescription = "交易对手分品种风险报告",
            retName = "RpcResponseListPaged<CounterPartyMarketRiskByUnderlyerReportDTO>",
            returnClass = CounterPartyMarketRiskByUnderlyerReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public RpcResponseListPaged<CounterPartyMarketRiskByUnderlyerReportDTO> rptSearchPagedCounterPartyMarketRiskByUnderlyerReport(
            @BctMethodArg String valuationDate,
            @BctMethodArg(required = false) Integer page,
            @BctMethodArg(required = false) Integer pageSize,
            @BctMethodArg(required = false) String partyNamePart,
            @BctMethodArg(required = false) String instrumentIdPart,
            @BctMethodArg(required = false) String order,
            @BctMethodArg(required = false) String orderBy) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        if (!Objects.isNull(page) || !Objects.isNull(pageSize)) {
            if (Objects.isNull(page) || Objects.isNull(pageSize))
                throw new IllegalArgumentException("当前页数和页面数量要同时不为空或同时为空");
        }

        Comparator<CounterPartyMarketRiskByUnderlyerReportDTO> comparator = new Comparator<CounterPartyMarketRiskByUnderlyerReportDTO>() {
            @Override
            public int compare(CounterPartyMarketRiskByUnderlyerReportDTO o1, CounterPartyMarketRiskByUnderlyerReportDTO o2) {
                Collator collator = Collator.getInstance(Locale.CHINESE);
                if (collator.compare(o1.getPartyName(), o2.getPartyName()) > 0){
                    return 1;
                }else if (collator.compare(o1.getPartyName(), o2.getPartyName()) < 0){
                    return -1;
                }
                return o1.getUnderlyerInstrumentId().compareTo(o2.getUnderlyerInstrumentId());
            }
        };
        orderBy = StringUtils.isEmpty(orderBy) ? "default" : orderBy;
        switch (orderBy) {
            case "underlyerInstrumentId":
                comparator = new Comparator<CounterPartyMarketRiskByUnderlyerReportDTO>() {
                    @Override
                    public int compare(CounterPartyMarketRiskByUnderlyerReportDTO o1, CounterPartyMarketRiskByUnderlyerReportDTO o2) {
                        Collator collator = Collator.getInstance(Locale.CHINESE);
                        if (collator.compare(o1.getUnderlyerInstrumentId(), o2.getUnderlyerInstrumentId()) > 0){
                            return 1;
                        }else if (collator.compare(o1.getUnderlyerInstrumentId(), o2.getUnderlyerInstrumentId()) < 0){
                            return -1;
                        }
                        return o1.getPartyName().compareTo(o2.getPartyName());
                    }
                };
                break;
            case "delta":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getDelta)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "deltaCash":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getDeltaCash)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "gamma":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getGamma)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "gammaCash":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getGammaCash)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "vega":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getVega)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "theta":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getTheta)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
            case "rho":
                comparator = Comparator.comparing(CounterPartyMarketRiskByUnderlyerReportDTO::getRho)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getPartyName)
                        .thenComparing(CounterPartyMarketRiskByUnderlyerReportDTO::getUnderlyerInstrumentId);
                break;
        }

        List<CounterPartyMarketRiskByUnderlyerReportDTO> reports = clientReportService
                .findAllCounterPartyMarketRiskByUnderlyerReportByDateAndInstrumentAndPartyName(LocalDate.parse(valuationDate), partyNamePart, instrumentIdPart);

        if ("default".equals(orderBy)){
            List<CounterPartyMarketRiskByUnderlyerReportDTO> cnList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getPartyName().substring(0, 1).toUpperCase()) == -1)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            List<CounterPartyMarketRiskByUnderlyerReportDTO> enList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getPartyName().substring(0, 1).toUpperCase()) != -1)
                    .sorted(comparator)
                    .collect(Collectors.toList());
            cnList.addAll(enList);
            reports = cnList;
        }else {
            reports = reports.stream().sorted(comparator).collect(Collectors.toList());
        }

        if ("desc".equals(order)) {
            Collections.reverse(reports);
        }
        page = Objects.isNull(page) ? 0 : page;
        pageSize = Objects.isNull(pageSize) ? Integer.MAX_VALUE : pageSize;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, reports.size());
        return new RpcResponseListPaged<>(reports.subList(start, end), reports.size());
    }

    @BctMethodInfo(
            description = "查看各子公司风险报告",
            retDescription = "各子公司风险报告",
            retName = "RpcResponseListPaged<SubsidiaryMarketRiskReportDTO>",
            returnClass = SubsidiaryMarketRiskReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public RpcResponseListPaged<SubsidiaryMarketRiskReportDTO> rptSearchPagedSubsidiaryMarketRiskReport(
            @BctMethodArg String valuationDate,
            @BctMethodArg(required = false) Integer page,
            @BctMethodArg(required = false) Integer pageSize,
            @BctMethodArg(required = false) String subsidiaryPart,
            @BctMethodArg(required = false) String order,
            @BctMethodArg(required = false) String orderBy) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        if (!Objects.isNull(page) || !Objects.isNull(pageSize)) {
            if (Objects.isNull(page) || Objects.isNull(pageSize))
                throw new IllegalArgumentException("当前页数和页面数量要同时不为空或同时为空");
        }
        final String orderedBy = StringUtils.isBlank(orderBy) ? "subsidiary" : orderBy;
        List<SubsidiaryMarketRiskReportDTO> reports = clientReportService
                .findAllSubsidiaryMarketRiskReportByDateAndSubsidiary(LocalDate.parse(valuationDate), subsidiaryPart);
        if (StringUtils.isBlank(orderBy)){
            List<SubsidiaryMarketRiskReportDTO> cnList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getSubsidiary().substring(0, 1).toUpperCase()) == -1)
                    .sorted((SubsidiaryMarketRiskReportDTO o1, SubsidiaryMarketRiskReportDTO o2)
                            -> {
                        Collator collator = Collator.getInstance(Locale.CHINESE);
                        if (collator.compare(o1.getSubsidiary(), o2.getSubsidiary()) > 0){
                            return 1;
                        }else if (collator.compare(o1.getSubsidiary(), o2.getSubsidiary()) < 0){
                            return -1;
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
            List<SubsidiaryMarketRiskReportDTO> enList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getSubsidiary().substring(0, 1).toUpperCase()) != -1)
                    .sorted(Comparator.comparing(SubsidiaryMarketRiskReportDTO::getSubsidiary))
                    .collect(Collectors.toList());
            cnList.addAll(enList);
            reports = cnList;
        }else {
            reports = reports.stream().sorted((item1, item2) -> {
                        int compareResult = 0;
                        if (orderedBy.equals("deltaCash")) {
                            compareResult = item1.getDeltaCash().compareTo(item2.getDeltaCash());
                        } else if (orderedBy.equals("gammaCash")) {
                            compareResult = item1.getGammaCash().compareTo(item2.getGammaCash());
                        } else if (orderedBy.equals("vega")) {
                            compareResult = item1.getVega().compareTo(item2.getVega());
                        } else if (orderedBy.equals("theta")) {
                            compareResult = item1.getTheta().compareTo(item2.getTheta());
                        } else if (orderedBy.equals("rho")) {
                            compareResult = item1.getRho().compareTo(item2.getRho());
                        } else {
                            compareResult = Collator.getInstance(Locale.CHINESE).compare(item1.getSubsidiary(), item2.getSubsidiary());
                        }
                        if (compareResult == 0) {
                            compareResult = item1.getCreatedAt().compareTo(item2.getCreatedAt());
                        }
                        return !StringUtils.isBlank(order) && order.equals("desc") ? -compareResult : compareResult;
                    }).collect(Collectors.toList());
        }
        page = Objects.isNull(page) ? 0 : page;
        pageSize = Objects.isNull(pageSize) ? Integer.MAX_VALUE : pageSize;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, reports.size());
        return new RpcResponseListPaged<>(reports.subList(start, end), reports.size());
    }

    @BctMethodInfo(
            description = "查看交易对手风险报告",
            retDescription = "交易对手风险报告",
            retName = "RpcResponseListPaged<CounterPartyMarketRiskReportDTO>",
            returnClass = CounterPartyMarketRiskReportDTO.class,
            service = "report-service",
            enableLogging = true
    )
    public RpcResponseListPaged<CounterPartyMarketRiskReportDTO> rptSearchPagedCounterPartyMarketRiskReport(
            @BctMethodArg String valuationDate,
            @BctMethodArg(required = false) Integer page,
            @BctMethodArg(required = false) Integer pageSize,
            @BctMethodArg(required = false) String partyNamePart,
            @BctMethodArg(required = false) String order,
            @BctMethodArg(required = false) String orderBy) {
        reportHelper.paramCheck(valuationDate, "请输入待查询计算日期");
        if (!Objects.isNull(page) || !Objects.isNull(pageSize)) {
            if (Objects.isNull(page) || Objects.isNull(pageSize))
                throw new IllegalArgumentException("当前页数和页面数量要同时不为空或同时为空");
        }
        final String orderedBy = StringUtils.isBlank(orderBy) ? "partyName" : orderBy;
        List<CounterPartyMarketRiskReportDTO> reports = clientReportService
                .findAllCounterPartyMarketRiskReportByDateAndPartyName(LocalDate.parse(valuationDate), partyNamePart);
        if (StringUtils.isBlank(orderBy)){
            List<CounterPartyMarketRiskReportDTO> cnList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getPartyName().substring(0, 1).toUpperCase()) == -1)
                    .sorted((CounterPartyMarketRiskReportDTO o1, CounterPartyMarketRiskReportDTO o2)
                            -> {
                        Collator collator = Collator.getInstance(Locale.CHINESE);
                        if (collator.compare(o1.getPartyName(), o2.getPartyName()) > 0){
                            return 1;
                        }else if (collator.compare(o1.getPartyName(), o2.getPartyName()) < 0){
                            return -1;
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
            List<CounterPartyMarketRiskReportDTO> enList = reports.stream()
                    .filter(v-> ReportConstants.EN_CHARACTER.indexOf(v.getPartyName().substring(0, 1).toUpperCase()) != -1)
                    .sorted(Comparator.comparing(CounterPartyMarketRiskReportDTO::getPartyName))
                    .collect(Collectors.toList());
            cnList.addAll(enList);
            reports = cnList;
        }else {
            reports = reports.stream().sorted((item1, item2) -> {
                int compareResult = 0;
                if (orderedBy.equals("deltaCash")) {
                    compareResult = item1.getDeltaCash().compareTo(item2.getDeltaCash());
                } else if (orderedBy.equals("gammaCash")) {
                    compareResult = item1.getGammaCash().compareTo(item2.getGammaCash());
                } else if (orderedBy.equals("vega")) {
                    compareResult = item1.getVega().compareTo(item2.getVega());
                } else if (orderedBy.equals("theta")) {
                    compareResult = item1.getTheta().compareTo(item2.getTheta());
                } else if (orderedBy.equals("rho")) {
                    compareResult = item1.getRho().compareTo(item2.getRho());
                } else {
                    compareResult = Collator.getInstance(Locale.CHINESE).compare(item1.getPartyName(), item2.getPartyName());
                }
                if (compareResult == 0) {
                    compareResult = item1.getCreatedAt().compareTo(item2.getCreatedAt());
                }
                return !StringUtils.isBlank(order) && order.equals("desc") ? -compareResult : compareResult;
            }).collect(Collectors.toList());
        }
        page = Objects.isNull(page) ? 0 : page;
        pageSize = Objects.isNull(pageSize) ? Integer.MAX_VALUE : pageSize;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, reports.size());
        return new RpcResponseListPaged<>(reports.subList(start, end), reports.size());
    }

    @BctMethodInfo(
            description = "更新日终汇总日盈亏报告",
            retDescription = "日终汇总日盈亏DTO",
            retName = "PnlReportDTO",
            returnClass = PnlReportDTO.class,
            service = "report-service"
    )
    public PnlReportDTO rptPnlReportUpdate(
            @BctMethodArg(description = "日终汇总日盈亏", argClass = PnlReportDTO.class) Map<String, Object> pnlReport
    ) {
        reportHelper.paramCheck(pnlReport, "请录入报告详情数据pnlReport");
        PnlReportDTO pnlReportDto = JsonUtils.mapper.convertValue(pnlReport, PnlReportDTO.class);
        return clientReportService.updatePnlById(pnlReportDto);
    }

    @BctMethodInfo(
            description = "更新日终汇总风险报告",
            retDescription = "日终汇总风险DTO",
            retName = "RiskReportDTO",
            returnClass = RiskReportDTO.class,
            service = "report-service"
    )
    public RiskReportDTO rptRiskReportUpdate(
            @BctMethodArg(description = "日终汇总风险", argClass = RiskReportDTO.class) Map<String, Object> riskReport
    ) {
        reportHelper.paramCheck(riskReport, "请录入报告详情数据riskReport");
        RiskReportDTO riskReportDto = JsonUtils.mapper.convertValue(riskReport, RiskReportDTO.class);
        return clientReportService.updateRiskById(riskReportDto);
    }

    @BctMethodInfo(
            description = "更新日终历史盈亏报告",
            retDescription = "日终历史盈亏DTO",
            retName = "PnlHstReportDTO",
            returnClass = PnlHstReportDTO.class,
            service = "report-service"
    )
    public PnlHstReportDTO rptPnlHstReportUpdate(
            @BctMethodArg(description = "日终历史盈亏", argClass = PnlHstReportDTO.class) Map<String, Object> pnlHstReport
    ) {
        reportHelper.paramCheck(pnlHstReport, "请录入报告详情数据pnlHstReport");
        PnlHstReportDTO pnlHstReportDto = JsonUtils.mapper.convertValue(pnlHstReport, PnlHstReportDTO.class);
        return clientReportService.updatePnlHstById(pnlHstReportDto);
    }

    @BctMethodInfo(
            description = "更新日终持仓明细报告",
            retDescription = "日终持仓明细DTO",
            retName = "PositionReportDTO",
            returnClass = PositionReportDTO.class,
            service = "report-service"
    )
    public PositionReportDTO rptPositonReportUpdate(
            @BctMethodArg(description = "日终持仓明细", argClass = PositionReportDTO.class) Map<String, Object> positionReport
    ) {
        reportHelper.paramCheck(positionReport, "请录入报告详情数据positionReport");
        PositionReportDTO positionReportDto = JsonUtils.mapper.convertValue(positionReport, PositionReportDTO.class);
        return clientReportService.updatePositionById(positionReportDto);
    }

    @BctMethodInfo(
            description = "更新交易报表",
            retDescription = "交易报表DTO",
            retName = "FinancialOtcTradeReportDTO",
            returnClass = FinancialOtcTradeReportDTO.class,
            service = "report-service"
    )
    public FinancialOtcTradeReportDTO rptOtcTradeReportUpdate(
            @BctMethodArg(description = "交易报表", argClass = FinancialOtcTradeReportDTO.class) Map<String, Object> otcTradeReport
    ) {
        reportHelper.paramCheck(otcTradeReport, "请录入报告详情数据pnlHstReport");
        FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO = JsonUtils.mapper.convertValue(otcTradeReport, FinancialOtcTradeReportDTO.class);
        return clientReportService.updateOtcTradeById(FinancialOTCTradeReportDTO);
    }

    @BctMethodInfo(
            description = "更新资金明细报表",
            retDescription = "资金明细报表DTO",
            retName = "FinancialOtcFundDetailReportDTO",
            returnClass = FinancialOtcFundDetailReportDTO.class,
            service = "report-service"
    )
    public FinancialOtcFundDetailReportDTO rptFinancialOtcFundDetailReportUpdate(
            @BctMethodArg(description = "资金明细报表", argClass = FinancialOtcFundDetailReportDTO.class) Map<String, Object> financialOtcFundDetailReport
    ) {
        reportHelper.paramCheck(financialOtcFundDetailReport, "请录入报告详情数据FinancialOtcFundDetailReport");
        FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO = JsonUtils.mapper.convertValue(financialOtcFundDetailReport, FinancialOtcFundDetailReportDTO.class);
        return clientReportService.updateFinancialOtcFundDetailById(financialOtcFundDetailReportDTO);
    }

    @BctMethodInfo(
            description = "更新客户资金汇总报表",
            retDescription = "客户资金汇总报表DTO",
            retName = "FinanicalOtcClientFundReportDTO",
            returnClass = FinanicalOtcClientFundReportDTO.class,
            service = "report-service"
    )
    public FinanicalOtcClientFundReportDTO rptFinanicalOtcClientFundReportUpdate(
            @BctMethodArg(description = "客户资金汇总报表", argClass = FinanicalOtcClientFundReportDTO.class) Map<String, Object> finanicalOtcClientFundReport
    ) {
        reportHelper.paramCheck(finanicalOtcClientFundReport, "请录入报告详情数据FinanicalOtcClientFundReport");
        FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO = JsonUtils.mapper.convertValue(finanicalOtcClientFundReport, FinanicalOtcClientFundReportDTO.class);
        return clientReportService.updateFinanicalOtcClientFundById(finanicalOtcClientFundReportDTO);
    }


    @BctMethodInfo(
            description = "分页获取日终汇总日盈亏报表",
            retDescription = "日终汇总日盈亏DTO",
            retName = "paged PnlReportDTO",
            returnClass = PnlReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PnlReportDTO> rptPnlReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "交易簿名称") String bookName,
            @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);

        List<String> bookIds = reportHelper.getReadableBook(true);
        PnlReportDTO template = new PnlReportDTO();

        if (!StringUtils.isBlank(bookName)) {
            if (bookIds.contains(bookName)) {
                template.setBookName(bookName);
            } else {
                throw new CustomException("不存在交易簿:" + bookName);
            }
        }

        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setUnderlyerInstrumentId(StringUtils.isBlank(underlyerInstrumentId) ? null : underlyerInstrumentId);

        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);
        List<PnlReportDTO> result = clientReportService.findPnlReportByTemplate(template, bookIds).stream()
                .sorted(field == ReportSearchableFieldEnum.BOOK_NAME ?
                        Comparator.comparing(PnlReportDTO::getBookName).
                                thenComparing(PnlReportDTO::getUnderlyerInstrumentId) :
                        Comparator.comparing(PnlReportDTO::getUnderlyerInstrumentId).
                                thenComparing(PnlReportDTO::getBookName)).
                        collect(Collectors.toList());

        if (order.equals("desc")) {
            Collections.reverse(result);
        }

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "分页获取日终汇总风险报表",
            retDescription = "日终汇总风险DTO",
            retName = "paged PnlReportDTOs",
            returnClass = PnlReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<RiskReportDTO> rptRiskReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "交易簿名称") String bookName,
            @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);

        List<String> bookIds = reportHelper.getReadableBook(true);
        RiskReportDTO template = new RiskReportDTO();

        if (!StringUtils.isBlank(bookName)) {
            if (bookIds.contains(bookName)) {
                template.setBookName(bookName);
            } else {
                throw new CustomException("不存在交易簿:" + bookName);
            }
        }

        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setUnderlyerInstrumentId(StringUtils.isBlank(underlyerInstrumentId) ? null : underlyerInstrumentId);

        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);

        List<RiskReportDTO> result = clientReportService.findRiskReportByTemplate(template, bookIds).stream()
                .sorted(field == ReportSearchableFieldEnum.BOOK_NAME ?
                        Comparator.comparing(RiskReportDTO::getBookName).
                                thenComparing(RiskReportDTO::getUnderlyerInstrumentId) :
                        Comparator.comparing(RiskReportDTO::getUnderlyerInstrumentId).
                                thenComparing(RiskReportDTO::getBookName)).
                        collect(Collectors.toList());

        if (order.equals("desc")) {
            Collections.reverse(result);
        }

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "分页获取日终历史盈亏报表",
            retDescription = "日终历史盈亏DTO",
            retName = "paged PnlHstReportDTO",
            returnClass = PnlHstReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PnlHstReportDTO> rptPnlHstReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "交易簿名称") String bookName,
            @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);

        List<String> bookIds = reportHelper.getReadableBook(true);
        PnlHstReportDTO template = new PnlHstReportDTO();

        if (!StringUtils.isBlank(bookName)) {
            if (bookIds.contains(bookName)) {
                template.setBookName(bookName);
            } else {
                throw new CustomException("不存在交易簿:" + bookName);
            }
        }

        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setUnderlyerInstrumentId(StringUtils.isBlank(underlyerInstrumentId) ? null : underlyerInstrumentId);

        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);

        List<PnlHstReportDTO> result = clientReportService.findPnlHstReportByTemplate(template, bookIds).stream()
                .sorted(field == ReportSearchableFieldEnum.BOOK_NAME ?
                        Comparator.comparing(PnlHstReportDTO::getBookName).
                                thenComparing(PnlHstReportDTO::getUnderlyerInstrumentId) :
                        Comparator.comparing(PnlHstReportDTO::getUnderlyerInstrumentId).
                                thenComparing(PnlHstReportDTO::getBookName)).
                        collect(Collectors.toList());

        if (order.equals("desc")) {
            Collections.reverse(result);
        }

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "按报告名获取离搜索日期的日终历史盈亏报告",
            retDescription = "日终历史盈亏DTO",
            retName = "List of PnlHstReportDTO",
            returnClass = PnlHstReportDTO.class,
            service = "report-service"
    )
    public List<PnlHstReportDTO> rptLatestPnlHstReportByNameAndDate(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请输入计算日期valuationDate");

        List<String> bookIds = reportHelper.getReadableBook(true);
        return clientReportService.findLatestPnlHstReportNameAndValuationDateAndBookId(reportName,
                bookIds,
                LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "按报告名获取离搜索日期的日终历史盈亏报告",
            retDescription = "日终历史盈亏DTO",
            retName = "List of PnlHstReportDTO",
            returnClass = PnlHstReportDTO.class,
            tags = {BctApiTagEnum.Excel},
            excelType = BctExcelTypeEnum.Table,
            service = "report-service"
    )
    public List<PnlHstReportDTO> rptLatestPnlHstReportByNameAndDateCreate(
            @BctMethodArg String reportName,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String valuationDate) {
        List<String> bookIds = reportHelper.getReadableBook(true);
        return clientReportService.findLatestPnlHstReportNameAndValuationDateAndBookId(
                reportName, bookIds, LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "分页获取日终持仓明细报表",
            retDescription = "日终持仓明细DTO",
            retName = "paged PositionReportDTO",
            returnClass = PositionReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PositionReportDTO> rptPositionReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "交易簿名称") String bookName,
            @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId,
            @BctMethodArg(required = false, description = "交易对手") String partyName,
            @BctMethodArg(required = false, description = "产品类型", argClass = ProductTypeEnum.class) String productType
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);
        List<String> bookIds = reportHelper.getReadableBook(true);
        PositionReportDTO template = new PositionReportDTO();

        if (!StringUtils.isBlank(bookName)) {
            if (bookIds.contains(bookName)) {
                template.setBookName(bookName);
            } else {
                throw new CustomException("不存在交易簿:" + bookName);
            }
        }

        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setPartyName(StringUtils.isBlank(partyName) ? null : partyName);
        template.setUnderlyerInstrumentId(StringUtils.isBlank(underlyerInstrumentId) ? null : underlyerInstrumentId);
        template.setProductType(StringUtils.isBlank(productType) ? null : ProductTypeEnum.valueOf(productType));

        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);

        List<PositionReportDTO> result = clientReportService.findPositionReportByTemplate(template, bookIds).stream()
                .sorted((item1, item2) -> {
                    int compareResult = 0;
                    if (field == ReportSearchableFieldEnum.TRADE_DATE) {
                        compareResult = item1.getTradeDate().compareTo(item2.getTradeDate());
                    } else if (field == ReportSearchableFieldEnum.EXPIRATION_DATE) {
                        compareResult = item1.getExpirationDate().compareTo(item2.getExpirationDate());
                    }
                    if (compareResult == 0) {
                        compareResult = item1.getCreatedAt().compareTo(item2.getCreatedAt());
                    }
                    return order.equals("asc") ? compareResult : -compareResult;
                }).collect(Collectors.toList());

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "按报告名获取离搜索日期的日终持仓明细报告",
            retDescription = "日终持仓明细DTO",
            retName = "List of PositionReportDTO",
            returnClass = PositionReportDTO.class,
            service = "report-service"
    )
    public List<PositionReportDTO> rptLatestPositionReportByNameAndDate(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(valuationDate, "请输入计算日期valuationDate");

        List<String> bookIds = reportHelper.getReadableBook(true);
        return clientReportService.findLatestPositionReportNameAndValuationDateAndBookId(
                reportName, bookIds, LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "分页获取资金明细报表",
            retDescription = "资金明细报表DTO",
            retName = "paged FinancialOtcTradeReportDTO",
            returnClass = FinancialOtcTradeReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<FinancialOtcTradeReportDTO> rptOtcTradeReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "客户名称") String client,
            @BctMethodArg(required = false, description = "标的资产名称") String baseContract,
            @BctMethodArg(required = false, description = "标的资产类型") String assetType,
            @BctMethodArg(required = false, description = "主协议编号") String masterAgreementId
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);
        List<String> bookIds = reportHelper.getReadableBook(true);

        FinancialOtcTradeReportDTO template = new FinancialOtcTradeReportDTO();
        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setClient(StringUtils.isBlank(client) ? null : client);
        template.setBaseContract(StringUtils.isBlank(baseContract) ? null : baseContract);
        template.setAssetType(StringUtils.isBlank(assetType) ? null : assetType);
        template.setMasterAgreementId(StringUtils.isBlank(masterAgreementId) ? null : masterAgreementId);

        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);
        List<FinancialOtcTradeReportDTO> result = clientReportService.findOtcTradeReportByTemplate(template, bookIds)
                .stream().sorted((item1, item2) -> {
                    int compareResult = 0;
                    if (field == ReportSearchableFieldEnum.DEAL_START_DATE) {
                        compareResult = item1.getDealStartDate().compareTo(item2.getDealStartDate());
                    } else if (field == ReportSearchableFieldEnum.EXPIRY) {
                        compareResult = item1.getExpiry().compareTo(item2.getExpiry());
                    }
                    if (compareResult == 0) {
                        compareResult = item1.getCreatedAt().compareTo(item2.getCreatedAt());
                    }
                    return order.equals("asc") ? compareResult : -compareResult;
                }).collect(Collectors.toList());

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "分页获取客户资金汇总报表",
            retDescription = "客户资金汇总报表DTO",
            retName = "paged FinancialOtcFundDetailReportDTO",
            returnClass = FinancialOtcFundDetailReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<FinancialOtcFundDetailReportDTO> rptFinancialOtcFundDetailReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "客户名称") String clientName,
            @BctMethodArg(required = false, description = "主协议编号") String masterAgreementId
    ) {

        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);

        FinancialOtcFundDetailReportDTO template = new FinancialOtcFundDetailReportDTO();
        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setClientName(StringUtils.isBlank(clientName) ? null : clientName);
        template.setMasterAgreementId(StringUtils.isBlank(masterAgreementId) ? null : masterAgreementId);

        List<FinancialOtcFundDetailReportDTO> result = clientReportService.findFinancialOtcFundDetailReportByTemplate(template).
                stream().sorted(order.equals("desc") ? Comparator.comparing(FinancialOtcFundDetailReportDTO::getPaymentDate).
                thenComparing(FinancialOtcFundDetailReportDTO::getCreatedAt).reversed() :
                Comparator.comparing(FinancialOtcFundDetailReportDTO::getPaymentDate).
                        thenComparing(FinancialOtcFundDetailReportDTO::getCreatedAt)).collect(Collectors.toList());

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "分页获取对冲端盈亏表",
            retDescription = "对冲端盈亏表DTO",
            retName = "paged HedgePnlReportDTO",
            returnClass = HedgePnlReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<HedgePnlReportDTO> rptHedgePnlReportPagedByNameAndDate(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);
        return clientReportService.findHedgePnlReportNameAndValuationDatePaged(reportName,
                LocalDate.parse(valuationDate), page, pageSize);
    }

    @BctMethodInfo(
            description = "分页获取风险指标统计表",
            retDescription = "风险指标统计表DTO",
            retName = "paged StatisticsOfRiskReportDTO",
            returnClass = StatisticsOfRiskReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<StatisticsOfRiskReportDTO> rptStatisticsOfRiskReportPagedByNameAndDate(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);
        return clientReportService.findStatisticsOfRiskReportNameAndValuationDatePaged(reportName,
                LocalDate.parse(valuationDate), page, pageSize);
    }

    @BctMethodInfo(
            description = "分页获取利润统计表",
            retDescription = "利润统计表DTO",
            retName = "paged ProfitStatisicsReportDTO",
            returnClass = ProfitStatisicsReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<ProfitStatisicsReportDTO> rptProfitStatisicsReportPagedByNameAndDate(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);
        return clientReportService.findProfitStatisicsReportNameAndValuationDatePaged(reportName,
                LocalDate.parse(valuationDate), page, pageSize);
    }

    @BctMethodInfo(
            description = "分页获取资产统计表",
            retDescription = "资产统计表DTO",
            retName = "paged SubjectComputingReportDTO",
            returnClass = SubjectComputingReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<SubjectComputingReportDTO> rptSubjectComputingReportPagedByNameAndDate(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate
    ) {
        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);
        return clientReportService.findSubjectComputingReportNameAndValuationDatePaged(reportName,
                LocalDate.parse(valuationDate), page, pageSize);
    }

    @BctMethodInfo(
            description = "分页获取资金明细报表",
            retDescription = "资金明细报表DTO",
            retName = "paged FinanicalOtcClientFundReportDTO",
            returnClass = FinanicalOtcClientFundReportDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<FinanicalOtcClientFundReportDTO> rptFinanicalOtcClientFundReportSearchPaged(
            @BctMethodArg(required = false, description = "页码") Integer page,
            @BctMethodArg(required = false, description = "页距") Integer pageSize,
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "生成日期") String valuationDate,
            @BctMethodArg(required = false, description = "客户名称") String clientName,
            @BctMethodArg(required = false, description = "主协议编号") String masterAgreementId
    ) {

        reportSearchParamsCheck(page, pageSize, reportName, valuationDate);

        FinanicalOtcClientFundReportDTO template = new FinanicalOtcClientFundReportDTO();
        template.setReportName(reportName);
        template.setValuationDate(LocalDate.parse(valuationDate));
        template.setClientName(StringUtils.isBlank(clientName) ? null : clientName);
        template.setMasterAgreementId(StringUtils.isBlank(masterAgreementId) ? null : masterAgreementId);

        List<FinanicalOtcClientFundReportDTO> result = clientReportService
                .findFinanicalOtcClientFundReportByTemplate(template).stream()
                .sorted(Comparator.comparing(FinanicalOtcClientFundReportDTO::getClientName)
                        .thenComparing(FinanicalOtcClientFundReportDTO::getCreatedAt)).collect(Collectors.toList());

        return reportHelper.paginateReport(page, pageSize, result);
    }

    @BctMethodInfo(
            description = "按交易对手查询客户估值报告",
            retDescription = "客户估值报告DTO",
            retName = "List of ValuationReportDTO",
            returnClass = ValuationReportDTO.class,
            service = "report-service"
    )
    public List<ValuationReportDTO> rptValuationReportList(
            @BctMethodArg(description = "交易对手名称列表") List<String> legalNames
    ) {
        reportHelper.paramCheck(legalNames, "请选择交易对手");
        return clientReportService.findValuationReportByLegalNames(legalNames);
    }

    @BctMethodInfo(
            description = "查询客户估值报告",
            retDescription = "客户估值报告DTO",
            retName = "List of ValuationReportDTO",
            returnClass = ValuationReportDTO.class,
            service = "report-service"
    )
    public List<ValuationReportDTO> rptValuationReportSearch(
            @BctMethodArg(required = false, description = "交易对手名称") String legalName,
            @BctMethodArg(required = false, description = "主协议编号") String masterAgreementId
    ) {
        ValuationReportDTO reportDto = new ValuationReportDTO();
        if (StringUtils.isNotBlank(masterAgreementId)) {
            PartyDTO party = partyService.getByMasterAgreementId(masterAgreementId);
            if (StringUtils.isNotBlank(legalName)) {
                if (!legalName.equals(party.getLegalName())) {
                    throw new CustomException(String.format("主协议编号:[%s],交易对手:[%s],客户信息不匹配",
                            masterAgreementId, legalName));
                }
            }
            legalName = party.getLegalName();
        }
        reportDto.setLegalName(StringUtils.isBlank(legalName) ? null : legalName);
        return clientReportService.valuationReportSearch(reportDto);
    }

    @BctMethodInfo(
            description = "发送客户估值报告",
            retDescription = "是否成功发送",
            retName = "success or exception")
    public String rptValuationReportSend(
            @BctMethodArg(description = "估值报告", argClass = ValuationReportDTO.class) List<Map<String, Object>> valuationReports
    ) {
        reportHelper.paramCheck(valuationReports, "估值报告为空");
        List<StringWriter> outs = valuationReports.stream().map(this::getDoc).collect(Collectors.toList());
        return "success";
    }

    @BctMethodInfo(description = "更新或新建客户估值报告")
    public void rptValuationReportCreateOrUpdate(
            @BctMethodArg(description = "估值报告", argClass = ValuationReportDTO.class) List<Map<String, Object>> valuationReports
    ) {
        reportHelper.paramCheck(valuationReports, "估值报告为空");
        valuationReports.forEach(valuation -> {
            ValuationReportDTO dto = JsonUtils.mapper.convertValue(valuation, ValuationReportDTO.class);
            clientReportService.createOrUpdateValuationReport(dto);
        });
    }

    private StringWriter getDoc(Map<String, Object> parm) {
        ValuationReportDTO dto = clientReportService.findValuationReportByLegalNameAndValuationDate(
                parm.get("legalName").toString(), LocalDate.parse(parm.get("valuationDate").toString()));
        BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTOByDocType(DocTypeEnum.VALUATION_REPORT);

        UUID dirId = bctTemplate.getDirectoryId();
        String group = bctTemplate.getGroupName();
        JsonNode data = dto.getContent();

        //fetch enabled template.
        TemplateDTO template = null;
        {
            TemplateDirectoryDTO dir = documentService.getDirectory(dirId)
                    .orElseThrow(() -> new IllegalArgumentException("invalid directory id."));

            List<TemplateDTO> templates = dir.getTemplates();
            if (null == templates || templates.size() < 1) {
                throw new IllegalArgumentException("templates in this directory is empty.");
            }

            template = templates.get(0);
        }

        String fullFileName = (template.getName().contains(".") ? template.getName().split("\\.")[0] :
                template.getName()) + "." + template.getTypeSuffix();

        StringWriter out = new StringWriter();

        documentService.genDocument(data, dirId, group, out);

        return out;
    }

    private void reportSearchParamsCheck(Integer page, Integer pageSize, String reportName, String valuationDate) {
        if (!Objects.isNull(page) || !Objects.isNull(pageSize)) {
            if (Objects.isNull(page)) throw new IllegalArgumentException("当前页数格式有问题page");
            if (Objects.isNull(pageSize)) throw new IllegalArgumentException("页面数量格式有问题pageSize");
        }

        if (StringUtils.isBlank(reportName)) throw new IllegalArgumentException("请输入报告名称reportName");
        if (StringUtils.isBlank(valuationDate)) throw new IllegalArgumentException("请输入计算日期valuationDate");
    }

    @BctMethodInfo(
            description = "查询主体估值报告标的列表",
            retDescription = "标的列表",
            retName = "success or exception")
    public List<String> rptReportInstrumentListByValuationDate(
            @BctMethodArg String valuationDate,
            @BctMethodArg String reportType
    ) {
        reportHelper.paramCheck(valuationDate, "估值日期为空");
        reportHelper.paramCheck(reportType, "报告类型为空");
        if (!ReportTypeEnum.isExist(reportType)){
            throw new CustomException("报告类型不支持");
        }
        return clientReportService.findReportInstrumentListByValuationDate(LocalDate.parse(valuationDate), ReportTypeEnum.valueOf(reportType));
    }

    @BctMethodInfo(
            description = "查询主体估值报告子公司列表",
            retDescription = "标的列表",
            retName = "success or exception")
    public List<String> rptReportSubsidiaryListByValuationDate(
            @BctMethodArg String valuationDate,
            @BctMethodArg String reportType
    ) {
        reportHelper.paramCheck(valuationDate, "估值日期为空");
        reportHelper.paramCheck(reportType, "报告类型为空");
        if (!ReportTypeEnum.isExist(reportType)){
            throw new CustomException("报告类型不支持");
        }
        return clientReportService.findReportSubsidiaryListByValuationDate(LocalDate.parse(valuationDate), ReportTypeEnum.valueOf(reportType));
    }

    @BctMethodInfo(
            description = "查询主体估值报告交易对手列表",
            retDescription = "标的列表",
            retName = "success or exception")
    public List<String> rptReportCounterPartyListByValuationDate(
            @BctMethodArg String valuationDate,
            @BctMethodArg String reportType
    ) {
        reportHelper.paramCheck(valuationDate, "估值日期为空");
        reportHelper.paramCheck(reportType, "报告类型为空");
        if (!ReportTypeEnum.isExist(reportType)){
            throw new CustomException("报告类型不支持");
        }
        return clientReportService.findReportCounterPartyListByValuationDate(LocalDate.parse(valuationDate), ReportTypeEnum.valueOf(reportType));
    }
}

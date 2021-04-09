package tech.tongyu.bct.report.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.api.client.ReportHelper;
import tech.tongyu.bct.report.api.client.ReportSearchableFieldEnum;
import tech.tongyu.bct.report.dto.GenericEodReportRowDTO;
import tech.tongyu.bct.report.dto.ReportTypeEnum;
import tech.tongyu.bct.report.dto.report.*;
import tech.tongyu.bct.report.service.EodReportService;
import tech.tongyu.bct.report.service.IntradayReportService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportApi {

    private EodReportService eodReportService;
    private IntradayReportService intradayReportService;
    private ReportHelper reportHelper;

    @Autowired
    public ReportApi(EodReportService eodReportService,
                     ResourceAuthAction resourceAuthAction,
                     IntradayReportService intradayReportService) {
        this.eodReportService = eodReportService;
        this.intradayReportService = intradayReportService;
        this.reportHelper = new ReportHelper(resourceAuthAction);
    }

    @BctMethodInfo(
            description = "获取所有日间自定义报告名",
            retDescription = "所有日间自定义报告名",
            retName = "List of intraday reprot names",
            service = "report-service"
    )
    public List<String> rptIntradayReportNamesList() {
        return new ArrayList<>(intradayReportService.listAllGenericIntradayReport()
                .stream().map(GenericIntradayReportRowDTO::getReportName).collect(Collectors.toSet()));
    }

    @BctMethodInfo(
            description = "根据报告名分页查询日间自定义报告",
            retDescription = "符合条件的日间自定义报告",
            retName = "paged GenericIntradayReportRowDTOs",
            returnClass = GenericIntradayReportRowDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<GenericIntradayReportRowDTO> rptIntradayReportPaged(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize
    ) {

        return reportHelper.filterAndPaginateReport(page, pageSize,
                () -> intradayReportService.listGenericIntradayReportByName(reportName));
    }

    @BctMethodInfo(
            description = "分页获取日间到期合约报告",
            retDescription = "日间到期合约报告",
            retName = "paged PositionExpiringReportRowDTOs",
            returnClass = PositionExpiringReportRowDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PositionExpiringReportRowDTO> rptIntradayTradeExpiringReportPaged(
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize,
            @BctMethodArg(description = "排序") String order
            ) {
        List<PositionExpiringReportRowDTO> result = reportHelper.filterReport(() ->
                intradayReportService.listAllIntradayTradeExpiringReport()).stream()
                .sorted(order.equals("desc") ? Comparator.comparing(PositionExpiringReportRowDTO :: getTradeDate).
                thenComparing(PositionExpiringReportRowDTO::getTradeId).reversed() :
                Comparator.comparing(PositionExpiringReportRowDTO :: getTradeDate).
                        thenComparing(PositionExpiringReportRowDTO::getTradeId)).collect(Collectors.toList());
        return reportHelper.paginateReport(page, pageSize,result);
    }

    @BctMethodInfo(
            description = "分页获取日间持仓明细报告",
            retDescription = "日间持仓明细报告",
            retName = "paged PositionReportRowDTOs",
            returnClass = PositionReportRowDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PositionReportRowDTO> rptIntradayTradeReportSearchPaged(
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "交易簿名称") String bookName,
            @BctMethodArg(required = false, description = "交易对手名称") String partyName,
            @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId,
            @BctMethodArg(required = false, description = "产品类型") String productType
    ) {
        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);
        List<PositionReportRowDTO> result = reportHelper.filterReport(() ->
                intradayReportService.listAllIntradayTradeReport()).stream().filter(item -> {
                    boolean filterResult = true;
                    if(!StringUtils.isBlank(bookName))
                        filterResult = filterResult && item.getBookName().equals(bookName);
                    if(!StringUtils.isBlank(partyName))
                        filterResult = filterResult && item.getPartyName().equals(partyName);
                    if(!StringUtils.isBlank(underlyerInstrumentId))
                        filterResult = filterResult && item.getUnderlyerInstrumentId().equals(underlyerInstrumentId);
                    if(!StringUtils.isBlank(productType))
                        filterResult = filterResult && item.getProductType().name().equals(productType);
                    return filterResult;
        }).sorted((item1,item2) -> {
            int compareResult = 0;
            if (field == ReportSearchableFieldEnum.TRADE_DATE){
                compareResult = item1.getTradeDate().compareTo(item2.getTradeDate());
            }else if(field == ReportSearchableFieldEnum.EXPIRATION_DATE){
                compareResult = item1.getExpirationDate().compareTo(item2.getExpirationDate());
            }
            if(compareResult == 0 ){
                compareResult = item1.getTradeId().compareTo(item2.getTradeId());
            }
            return order.equals("asc") ? compareResult : -compareResult;
        }).collect(Collectors.toList());
        return reportHelper.paginateReport(page, pageSize,result);
    }

    @BctMethodInfo(
            description = "分页获取日间标的风险报告",
            retDescription = "日间标的风险报告",
            retName = "paged RiskReportRowDTOs",
            returnClass = RiskReportRowDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<RiskReportRowDTO> rptIntradayRiskReportSearchPaged(
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize,
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order,
            @BctMethodArg(required = false, description = "交易簿名称") String bookName,
            @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId
    ) {
        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);

        List<RiskReportRowDTO> result = reportHelper.filterReport(() -> intradayReportService.listAllIntradayRiskReport())
                .stream().filter(item -> {
            boolean filterResult = true;
            if(!StringUtils.isBlank(bookName))
                filterResult = filterResult && item.getBookName().equals(bookName);
            if(!StringUtils.isBlank(underlyerInstrumentId))
                filterResult = filterResult && item.getUnderlyerInstrumentId().equals(underlyerInstrumentId);
            return filterResult;
        }).sorted(field == ReportSearchableFieldEnum.BOOK_NAME ?
                Comparator.comparing(RiskReportRowDTO :: getBookName).
                        thenComparing(RiskReportRowDTO::getUnderlyerInstrumentId):
                Comparator.comparing(RiskReportRowDTO :: getUnderlyerInstrumentId).
                        thenComparing(RiskReportRowDTO::getBookName)).collect(Collectors.toList());
        if(order.equals("desc")){
            Collections.reverse(result);
        }
        return reportHelper.paginateReport(page, pageSize,result);
    }

    @BctMethodInfo(
            description = "分页获取日间标的盈亏报告",
            retDescription = "日间标的盈亏报告",
            retName = "paged PnlReportRowDTOs",
            returnClass = PnlReportRowDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PnlReportRowDTO> rptIntradayPnlReportSearchPaged
            (@BctMethodArg(description = "当前页数", required = false) Integer page,
             @BctMethodArg(description = "每页数量", required = false) Integer pageSize,
             @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
             @BctMethodArg(description = "排序") String order,
             @BctMethodArg(required = false, description = "交易簿名称") String bookName,
             @BctMethodArg(required = false, description = "标的物代码") String underlyerInstrumentId
            ) {
        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);

        List<PnlReportRowDTO> result = reportHelper.filterReport(() -> intradayReportService.listAllIntradayPnlReport()).stream().
                filter(item -> {
                    boolean filterResult = true;
                    if(!StringUtils.isBlank(bookName))
                        filterResult = filterResult && item.getBookName().equals(bookName);
                    if(!StringUtils.isBlank(underlyerInstrumentId))
                        filterResult = filterResult && item.getUnderlyerInstrumentId().equals(underlyerInstrumentId);
                    return filterResult;
                }).sorted(field == ReportSearchableFieldEnum.BOOK_NAME ?
                Comparator.comparing(PnlReportRowDTO :: getBookName).
                        thenComparing(PnlReportRowDTO::getUnderlyerInstrumentId):
                Comparator.comparing(PnlReportRowDTO :: getUnderlyerInstrumentId).
                        thenComparing(PnlReportRowDTO::getBookName)).collect(Collectors.toList());
        if(order.equals("desc")){
            Collections.reverse(result);
        }
        return reportHelper.paginateReport(page, pageSize,result);
    }

    @BctMethodInfo(
            description = "获取日内盈亏报告",
            retDescription = "日内盈亏报告",
            retName = "intraday pnl report",
            tags = {BctApiTagEnum.Excel},
            excelType = BctExcelTypeEnum.Table,
            service = "report-service"
    )
    public List<Map<String, Object>> rptIntradayPnlReportGet(
            @BctMethodArg(description = "排序关键词", argClass = ReportSearchableFieldEnum.class) String orderBy,
            @BctMethodArg(description = "排序") String order
    ){
        ReportSearchableFieldEnum field = ReportSearchableFieldEnum.of(orderBy);
        List<PnlReportRowDTO> dtoList = reportHelper.filterReport(() ->  intradayReportService.listAllIntradayPnlReport().stream().
                filter(item ->{
                    boolean filterResult = true;
                    return filterResult;
                }).sorted(field == ReportSearchableFieldEnum. BOOK_NAME ?
                Comparator.comparing(PnlReportRowDTO :: getBookName).
                        thenComparing(PnlReportRowDTO::getUnderlyerInstrumentId):
                Comparator.comparing(PnlReportRowDTO :: getUnderlyerInstrumentId).
                        thenComparing(PnlReportRowDTO::getBookName)).collect(Collectors.toList()));
        if(order.equals("desc")){
            Collections.reverse(dtoList);
        }
        return dtoList.stream().map(dto -> {
            Map<String,Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put("bookName", dto.getBookName());
            stringObjectMap.put("underlyerInstrumentId", dto.getUnderlyerInstrumentId());
            stringObjectMap.put("dailyPnl", dto.getDailyPnl().doubleValue());
            stringObjectMap.put("dailyOptionPnl", dto.getDailyOptionPnl().doubleValue());
            stringObjectMap.put("dailyUnderlyerPnl", dto.getDailyUnderlyerPnl().doubleValue());
            stringObjectMap.put("pnlContributionNew", dto.getPnlContributionNew().doubleValue());
            stringObjectMap.put("pnlContributionSettled", dto.getPnlContributionSettled().doubleValue());
            stringObjectMap.put("pnlContributionDelta", dto.getPnlContributionDelta().doubleValue());
            stringObjectMap.put("pnlContributionGamma", dto.getPnlContributionGamma().doubleValue());
            stringObjectMap.put("pnlContributionVega", dto.getPnlContributionVega().doubleValue());
            stringObjectMap.put("pnlContributionTheta", dto.getPnlContributionTheta().doubleValue());
            stringObjectMap.put("pnlContributionRho", dto.getPnlContributionRho().doubleValue());
            stringObjectMap.put("pnlContributionUnexplained", dto.getPnlContributionUnexplained().doubleValue());
            return stringObjectMap;
        }).collect(Collectors.toList());
    }


    @BctMethodInfo(
            description = "分页获取日间投资组合风险报告",
            retDescription = "日间投资组合风险报告",
            retName = "paged PortfolioRiskReportRowDTOs",
            returnClass = PortfolioRiskReportRowDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<PortfolioRiskReportRowDTO> rptIntradayPortfolioRiskReportSearchPaged
            (@BctMethodArg(description = "当前页数", required = false) Integer page,
             @BctMethodArg(description = "每页数量", required = false) Integer pageSize,
             @BctMethodArg(required = false, description = "投资组合名称") String portfolioName) {
        List<PortfolioRiskReportRowDTO> reports = intradayReportService.listAllPortfolioRiskReport().stream()
                .filter(item-> StringUtils.isBlank(portfolioName) || item.getPortfolioName().equals(portfolioName))
                .sorted(Comparator.comparing(PortfolioRiskReportRowDTO::getPortfolioName)).collect(Collectors.toList());
        return reportHelper.paginateReport(page, pageSize, reports);
    }


    @BctMethodInfo(
            description = "创建一条日终报告数据",
            retDescription = "是否成功创建",
            retName = "true or false",
            service = "report-service"
    )
    public Boolean rptEodReportCreate(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "报告类型", argClass = ReportTypeEnum.class) String reportType,
            @BctMethodArg(description = "报告修改人") String modifiedBy,
            @BctMethodArg(description = "计算日期") String valuationDate,
            @BctMethodArg(description = "一条日终报告数据", argClass = GenericEodReportRowDTO.class) List<Map<String, Object>> reportData
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(reportType, "请录入报告类型reportType");
        reportHelper.paramCheck(modifiedBy, "请录入报告修改人modifiedBy");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        reportHelper.paramCheck(reportData, "请录入报告详情数据reportData");

        eodReportService.createReport(reportName, ReportTypeEnum.valueOf(reportType),
                LocalDate.parse(valuationDate), modifiedBy, reportData);
        return true;
    }

    @BctMethodInfo(
            description = "修改一条指定日终报告",
            retDescription = "修改后报告",
            retName = "GenericEodReportRowDTO",
            returnClass = GenericEodReportRowDTO.class,
            service = "report-service"
    )
    public GenericEodReportRowDTO rptEodReportUpdate(
            @BctMethodArg(description = "一条指定日终报告", argClass = GenericEodReportRowDTO.class) Map<String, Object> eodReportRow
    ) {
        if (CollectionUtils.isEmpty(eodReportRow)) {
            throw new IllegalArgumentException("请录入报告详情数据eodReportRow");
        }
        GenericEodReportRowDTO eodReportRowDto = JsonUtils.mapper.
                convertValue(eodReportRow, GenericEodReportRowDTO.class);
        return eodReportService.saveReportRow(eodReportRowDto);
    }

    @BctMethodInfo(
            description = "删除一条指定报告",
            retDescription = "是否成功删除",
            retName = "true or false",
            service = "report-service"
    )
    public Boolean rptEodReportDelete(
            @BctMethodArg(description = "报告ID") String reportId
    ) {
        reportHelper.paramCheck(reportId, "缺少待删除报告记录编号reportId");
        eodReportService.deleteReport(reportId);
        return true;
    }

    @BctMethodInfo(
            description = "删除符合条件的报告信息",
            retDescription = "是否成功删除",
            retName = "true or false",
            service = "report-service"
    )
    public Boolean rptEodReportDeleteAll(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "报告类型", argClass = ReportTypeEnum.class) String reportType,
            @BctMethodArg(description = "计算日期") String valuationDate
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(reportType, "请录入报告类型reportType");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        eodReportService.deleteByNameAndTypeAndDate(reportName, ReportTypeEnum.valueOf(reportType),
                LocalDate.parse(valuationDate));
        return true;
    }

    @BctMethodInfo(
            description = "获取符合日期的报告结果集",
            retDescription = "符合日期的报告结果集",
            retName = "GenericEodReportRowDTO",
            returnClass = GenericEodReportRowDTO.class,
            service = "report-service"
    )
    public List<GenericEodReportRowDTO> rptEodReportListByDate(
            @BctMethodArg(description = "计算日期") String valuationDate
    ) {
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");
        return eodReportService.findByValuationDate(LocalDate.parse(valuationDate));
    }

    @BctMethodInfo(
            description = "获取符合条件的报告结果集",
            retDescription = "符合条件的报告结果集",
            retName = "List of GenericEodReportRowDTO",
            returnClass = GenericEodReportRowDTO.class,
            service = "report-service"
    )
    public List<GenericEodReportRowDTO> rptEodReportSearch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "报告类型", argClass = ReportTypeEnum.class) String reportType,
            @BctMethodArg(description = "计算日期") String valuationDate
    ) {
        reportHelper.paramCheck(reportName, "请录入报告名称reportName");
        reportHelper.paramCheck(reportType, "请录入报告类型reportType");
        reportHelper.paramCheck(valuationDate, "请录入报告计算日期valuationDate");

        return eodReportService.getReportByNameAndTypeAndDate(reportName, ReportTypeEnum.valueOf(reportType),
                LocalDate.parse(valuationDate));
    }
}

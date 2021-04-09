package tech.tongyu.bct.report.service.client.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.PartyService;
import tech.tongyu.bct.report.client.dto.*;
import tech.tongyu.bct.report.client.service.ClientReportService;
import tech.tongyu.bct.report.dao.client.dbo.*;
import tech.tongyu.bct.report.dao.client.repo.*;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.ReportTypeEnum;
import tech.tongyu.bct.report.dto.report.MarketScenarioType;

import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientReportServiceImpl implements ClientReportService {

    private ValuationReportRepo valuationRepo;

    private PositionReportRepo positionRepo;

    private PnlHstReportRepo pnlHstRepo;

    private RiskReportRepo riskRepo;

    private PnlReportRepo pnlRepo;

    private PartyService partyService;

    private FinancialOtcTradeReportRepo otcTradeRepo;

    private FinancialOtcFundDetailReportRepo fodRepo;

    private FinanicalOtcClientFundReportRepo focRepo;

    private SubjectComputingReportRepo subjectComputingReportRepo;

    private ProfitStatisicsReportRepo profitStatisicsReportRepo;

    private StatisticsOfRiskReportRepo statisticsOfRiskReportRepo;

    private HedgePnlReportRepo hedgePnlReportRepo;

    private MarketRiskReportRepo marketRiskReportRepo;

    private MarketRiskDetailReportRepo marketRiskDetailReportRepo;

    private MarketRiskBySubUnderlyerReportRepo marketRiskBySubUnderlyerReportRepo;

    private SpotScenariosReportRepo spotScenariosReportRepo;

    private SubsidiaryMarketRiskReportRepo subsidiaryMarketRiskReportRepo;

    private CounterPartyMarketRiskReportRepo counterPartyMarketRiskReportRepo;

    private CounterPartyMarketRiskByUnderlyerReportRepo counterPartyMarketRiskByUnderlyerReportRepo;

    @Autowired
    public ClientReportServiceImpl(ValuationReportRepo valuationRepo,
                                   PositionReportRepo positionRepo,
                                   PnlHstReportRepo pnlHstRepo,
                                   RiskReportRepo riskRepo,
                                   PnlReportRepo pnlRepo,
                                   PartyService partyService,
                                   FinancialOtcTradeReportRepo otcTradeRepo,
                                   FinancialOtcFundDetailReportRepo fodRepo,
                                   FinanicalOtcClientFundReportRepo focRepo,
                                   SubjectComputingReportRepo subjectComputingReportRepo,
                                   ProfitStatisicsReportRepo profitStatisicsReportRepo,
                                   StatisticsOfRiskReportRepo statisticsOfRiskReportRepo,
                                   HedgePnlReportRepo hedgePnlReportRepo,
                                   MarketRiskReportRepo marketRiskReportRepo,
                                   MarketRiskDetailReportRepo marketRiskDetailReportRepo,
                                   SubsidiaryMarketRiskReportRepo subsidiaryMarketRiskReportRepo,
                                   CounterPartyMarketRiskReportRepo counterPartyMarketRiskReportRepo,
                                   MarketRiskBySubUnderlyerReportRepo marketRiskBySubUnderlyerReportRepo,
                                   CounterPartyMarketRiskByUnderlyerReportRepo counterPartyMarketRiskByUnderlyerReportRepo,
                                   SpotScenariosReportRepo spotScenariosReportRepo) {
        this.valuationRepo = valuationRepo;
        this.positionRepo = positionRepo;
        this.pnlHstRepo = pnlHstRepo;
        this.riskRepo = riskRepo;
        this.pnlRepo = pnlRepo;
        this.otcTradeRepo = otcTradeRepo;
        this.fodRepo = fodRepo;
        this.focRepo = focRepo;
        this.subjectComputingReportRepo = subjectComputingReportRepo;
        this.profitStatisicsReportRepo = profitStatisicsReportRepo;
        this.statisticsOfRiskReportRepo = statisticsOfRiskReportRepo;
        this.hedgePnlReportRepo = hedgePnlReportRepo;
        this.partyService = partyService;
        this.marketRiskReportRepo = marketRiskReportRepo;
        this.marketRiskDetailReportRepo = marketRiskDetailReportRepo;
        this.marketRiskBySubUnderlyerReportRepo = marketRiskBySubUnderlyerReportRepo;
        this.spotScenariosReportRepo = spotScenariosReportRepo;
        this.subsidiaryMarketRiskReportRepo = subsidiaryMarketRiskReportRepo;
        this.counterPartyMarketRiskReportRepo = counterPartyMarketRiskReportRepo;
        this.counterPartyMarketRiskByUnderlyerReportRepo = counterPartyMarketRiskByUnderlyerReportRepo;
    }

    @Override
    @Transactional
    public void createPnlReport(PnlReportDTO pnlReportDto) {
        if (Objects.isNull(pnlReportDto)) {
            throw new CustomException("盈亏归因数据不存在");
        }
        PnlReport pnlReport = ReportDtoDboConvertor.transToDbo(pnlReportDto);
        pnlRepo.deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(pnlReport.getBookName(),
                pnlReport.getReportName(), pnlReport.getUnderlyerInstrumentId(), pnlReport.getValuationDate());
        pnlRepo.save(pnlReport);
    }

    @Override
    @Transactional
    public void createRiskReport(RiskReportDTO riskReportDto) {
        if (Objects.isNull(riskReportDto)) {
            throw new CustomException("风险报告数据不存在");
        }
        RiskReport riskReport = ReportDtoDboConvertor.transToDbo(riskReportDto);
        riskRepo.deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(riskReport.getBookName(),
                riskReport.getReportName(), riskReport.getUnderlyerInstrumentId(), riskReport.getValuationDate());
        riskRepo.save(riskReport);
    }

    @Override
    @Transactional
    public void createPnlHstReport(PnlHstReportDTO pnlHstReportDto) {
        if (Objects.isNull(pnlHstReportDto)) {
            throw new CustomException("历史盈亏数据不存在");
        }
        PnlHstReport pnlHstReport = ReportDtoDboConvertor.transToDbo(pnlHstReportDto);
        pnlHstRepo.deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(pnlHstReport.getBookName(),
                pnlHstReport.getReportName(), pnlHstReport.getUnderlyerInstrumentId(), pnlHstReport.getValuationDate());
        pnlHstRepo.save(pnlHstReport);
    }

    @Override
    @Transactional
    public void createPositionReport(PositionReportDTO positionReportDto) {
        if (Objects.isNull(positionReportDto)) {
            throw new CustomException("交易详情数据不存在");
        }
        PositionReport positionReport = ReportDtoDboConvertor.transToDbo(positionReportDto);
        positionRepo.deleteByReportNameAndPositionIdAndValuationDate(
                positionReportDto.getReportName(), positionReport.getPositionId(), positionReport.getValuationDate());
        positionRepo.save(positionReport);
    }

    @Override
    @Transactional
    public void createOtcTradeReport(FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO) {
        if (Objects.isNull(FinancialOTCTradeReportDTO)) {
            throw new CustomException("交易详情数据不存在");
        }
        FinancialOtcTradeReport FinancialOTCTradeReport = ReportDtoDboConvertor.transToDbo(FinancialOTCTradeReportDTO);
        otcTradeRepo.deleteByReportNameAndValuationDateAndOptionName(
                FinancialOTCTradeReportDTO.getReportName(), FinancialOTCTradeReportDTO.getValuationDate(), FinancialOTCTradeReportDTO.getOptionName());
        otcTradeRepo.save(FinancialOTCTradeReport);
    }

    @Override
    @Transactional
    public void createFinancialOtcFundDetailReport(FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO) {
        if (Objects.isNull(financialOtcFundDetailReportDTO)) {
            throw new CustomException("交易详情数据不存在");
        }
        FinancialOtcFundDetailReport financialOtcFundDetailReport = ReportDtoDboConvertor.transToDbo(financialOtcFundDetailReportDTO);
        fodRepo.save(financialOtcFundDetailReport);
    }

    @Override
    @Transactional
    public void createFinanicalOtcClientFundReport(FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO) {
        if (Objects.isNull(finanicalOtcClientFundReportDTO)) {
            throw new CustomException("场外期权业务客户资金数据不存在");
        }
        FinanicalOtcClientFundReport finanicalOtcClientFundReport = ReportDtoDboConvertor.transToDbo(finanicalOtcClientFundReportDTO);
        focRepo.deleteByReportNameAndClientNameAndValuationDate(
                finanicalOtcClientFundReportDTO.getReportName(), finanicalOtcClientFundReportDTO.getClientName(), finanicalOtcClientFundReportDTO.getValuationDate());
        focRepo.save(finanicalOtcClientFundReport);
    }

    @Override
    public void createHedgePnlReport(HedgePnlReportDTO hedgePnlReportDTO) {
        HedgePnlReport hedgePnlReport = ReportDtoDboConvertor.transToDbo(hedgePnlReportDTO);
        hedgePnlReportRepo.save(hedgePnlReport);
    }

    @Override
    public void createProfitStatisicsReport(ProfitStatisicsReportDTO profitStatisicsReportDTO) {
        ProfitStatisicsReport profitStatisicsReport = ReportDtoDboConvertor.transToDbo(profitStatisicsReportDTO);
        profitStatisicsReportRepo.save(profitStatisicsReport);
    }

    @Override
    public void createStatisticsOfRiskReport(StatisticsOfRiskReportDTO statisticsOfRiskReportDTO) {
        StatisticsOfRiskReport statisticsOfRiskReport = ReportDtoDboConvertor.transToDbo(statisticsOfRiskReportDTO);
        statisticsOfRiskReportRepo.save(statisticsOfRiskReport);
    }

    @Override
    public void createSubjectComputingReport(SubjectComputingReportDTO subjectComputingReportDTO) {
        SubjectComputingReport subjectComputingReport = ReportDtoDboConvertor.transToDbo(subjectComputingReportDTO);
        subjectComputingReportRepo.save(subjectComputingReport);
    }

    @Override
    @Transactional
    public void createMarketRiskReport(MarketRiskReportDTO marketRiskReportDTO) {
        if (Objects.isNull(marketRiskReportDTO)) {
            throw new CustomException("全市场整体风险汇总报告数据不存在");
        }
        MarketRiskReport report = ReportDtoDboConvertor.transToDbo(marketRiskReportDTO);
        marketRiskReportRepo.deleteAllByReportNameAndValuationDate(report.getReportName(), report.getValuationDate());
        marketRiskReportRepo.save(report);
    }

    @Override
    @Transactional
    public void createMarketRiskDetailReport(MarketRiskDetailReportDTO marketRiskDetailReportDTO) {
        if (Objects.isNull(marketRiskDetailReportDTO)) {
            throw new CustomException("全市场分品种风险报告数据不存在");
        }
        MarketRiskDetailReport rpt = ReportDtoDboConvertor.transToDbo(marketRiskDetailReportDTO);
        marketRiskDetailReportRepo.deleteByValuationDateAndUnderlyerInstrumentIdAndScenarioTypeAndReportTypeAndSubsidiaryAndPartyName(
                rpt.getValuationDate(), rpt.getUnderlyerInstrumentId(), rpt.getScenarioType(), rpt.getReportType(), rpt.getSubsidiary(), rpt.getPartyName());
        marketRiskDetailReportRepo.save(rpt);
    }

    @Override
    @Transactional
    public void createSubsidiaryMarketRiskReport(SubsidiaryMarketRiskReportDTO subsidiaryMarketRiskReportDTO) {
        if (Objects.isNull(subsidiaryMarketRiskReportDTO)) {
            throw new CustomException("各子公司整体风险报告数据不存在");
        }
        SubsidiaryMarketRiskReport report = ReportDtoDboConvertor.transToDbo(subsidiaryMarketRiskReportDTO);
        subsidiaryMarketRiskReportRepo.deleteByValuationDateAndSubsidiary(report.getValuationDate(), report.getSubsidiary());
        subsidiaryMarketRiskReportRepo.save(report);
    }

    @Override
    @Transactional
    public void createCounterPartyMarketRiskReport(CounterPartyMarketRiskReportDTO counterPartyMarketRiskReportDTO) {
        if (Objects.isNull(counterPartyMarketRiskReportDTO)) {
            throw new CustomException("各子公司整体风险报告数据不存在");
        }
        CounterPartyMarketRiskReport report = ReportDtoDboConvertor.transToDbo(counterPartyMarketRiskReportDTO);
        counterPartyMarketRiskReportRepo.deleteByValuationDateAndPartyName(report.getValuationDate(), report.getPartyName());
        counterPartyMarketRiskReportRepo.save(report);
    }

    @Override
    @Transactional
    public void createCounterPartyMarketRiskByUnderlyerReport(CounterPartyMarketRiskByUnderlyerReportDTO reportDTO) {
        if (Objects.isNull(reportDTO)) {
            throw new CustomException("报告数据不存在");
        }
        CounterPartyMarketRiskByUnderlyerReport report = ReportDtoDboConvertor.transToDbo(reportDTO);
        counterPartyMarketRiskByUnderlyerReportRepo.deleteByValuationDateAndPartyNameAndUnderlyerInstrumentId(report.getValuationDate(), report.getPartyName(), report.getUnderlyerInstrumentId());
        counterPartyMarketRiskByUnderlyerReportRepo.save(report);
    }

    @Override
    @Transactional
    public void createMarketRiskBySubUnderlyerReport(MarketRiskBySubUnderlyerReportDTO riskReportDto) {
        if (Objects.isNull(riskReportDto)) {
            throw new CustomException("报告数据不存在");
        }
        MarketRiskBySubUnderlyerReport riskReport = ReportDtoDboConvertor.transToDbo(riskReportDto);
        marketRiskBySubUnderlyerReportRepo.deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(riskReport.getBookName(),
                riskReport.getReportName(), riskReport.getUnderlyerInstrumentId(), riskReport.getValuationDate());
        marketRiskBySubUnderlyerReportRepo.save(riskReport);
    }

    @Override
    @Transactional
    public void createSpotScenariosReport(SpotScenariosReportDTO dto) {
        if (Objects.isNull(dto)) {
            throw new CustomException("报告数据不存在");
        }
        SpotScenariosReport report = ReportDtoDboConvertor.transToDbo(dto);
        spotScenariosReportRepo.deleteByValuationDateAndInstrumentIdAndReportTypeAndContentName(
                report.getValuationDate(), report.getInstrumentId(), report.getReportType(), report.getContentName());
        spotScenariosReportRepo.save(report);
    }

    @Override
    @Transactional
    public PnlReportDTO updatePnlById(PnlReportDTO pnlReportDto) {
        if (StringUtils.isBlank(pnlReportDto.getUuid())) {
            throw new IllegalStateException("请输入盈亏归因唯一标识");
        }
        PnlReport pnlReport = ReportDtoDboConvertor.transToDbo(pnlReportDto);
        pnlRepo.findById(pnlReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", pnlReportDto.getUuid())));
        pnlRepo.save(pnlReport);
        return pnlReportDto;
    }

    @Override
    @Transactional
    public RiskReportDTO updateRiskById(RiskReportDTO riskReportDto) {
        if (StringUtils.isBlank(riskReportDto.getUuid())) {
            throw new IllegalStateException("请输入风险报告唯一标识");
        }
        RiskReport riskReport = ReportDtoDboConvertor.transToDbo(riskReportDto);
        riskRepo.findById(riskReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", riskReportDto.getUuid())));
        riskRepo.save(riskReport);
        return riskReportDto;
    }

    @Override
    @Transactional
    public PnlHstReportDTO updatePnlHstById(PnlHstReportDTO pnlHstReportDto) {
        if (StringUtils.isBlank(pnlHstReportDto.getUuid())) {
            throw new IllegalStateException("请输入历史盈亏唯一标识");
        }
        PnlHstReport pnlHstReport = ReportDtoDboConvertor.transToDbo(pnlHstReportDto);
        pnlHstRepo.findById(pnlHstReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", pnlHstReportDto.getUuid())));
        return pnlHstReportDto;
    }

    @Override
    @Transactional
    public PositionReportDTO updatePositionById(PositionReportDTO positionReportDto) {
        if (StringUtils.isBlank(positionReportDto.getUuid())) {
            throw new IllegalStateException("请输入交易报告唯一标识");
        }
        PositionReport positionReport = ReportDtoDboConvertor.transToDbo(positionReportDto);
        positionRepo.findById(positionReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", positionReportDto.getUuid())));
        return positionReportDto;
    }

    @Override
    @Transactional
    public FinancialOtcTradeReportDTO updateOtcTradeById(FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO) {
        if (StringUtils.isBlank(FinancialOTCTradeReportDTO.getUuid())) {
            throw new IllegalStateException("请输入交易报告唯一标识");
        }
        FinancialOtcTradeReport FinancialOTCTradeReport = ReportDtoDboConvertor.transToDbo(FinancialOTCTradeReportDTO);
        otcTradeRepo.findById(FinancialOTCTradeReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", FinancialOTCTradeReportDTO.getUuid())));
        otcTradeRepo.save(FinancialOTCTradeReport);
        return FinancialOTCTradeReportDTO;
    }

    @Override
    @Transactional
    public FinancialOtcFundDetailReportDTO updateFinancialOtcFundDetailById(FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO) {
        if (StringUtils.isBlank(financialOtcFundDetailReportDTO.getUuid())) {
            throw new IllegalStateException("请输入交易报告唯一标识");
        }
        FinancialOtcFundDetailReport financialOtcFundDetailReport = ReportDtoDboConvertor.transToDbo(financialOtcFundDetailReportDTO);
        fodRepo.findById(financialOtcFundDetailReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", financialOtcFundDetailReportDTO.getUuid())));
        fodRepo.save(financialOtcFundDetailReport);
        return financialOtcFundDetailReportDTO;
    }

    @Override
    @Transactional
    public FinanicalOtcClientFundReportDTO updateFinanicalOtcClientFundById(FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO) {
        if (StringUtils.isBlank(finanicalOtcClientFundReportDTO.getUuid())) {
            throw new IllegalStateException("请输入交易报告唯一标识");
        }
        FinanicalOtcClientFundReport finanicalOtcClientFundReport = ReportDtoDboConvertor.transToDbo(finanicalOtcClientFundReportDTO);
        focRepo.findById(finanicalOtcClientFundReport.getUuid())
                .orElseThrow(() -> new IllegalStateException(String.format("报告标识:%s,记录不存在", finanicalOtcClientFundReportDTO.getUuid())));
        focRepo.save(finanicalOtcClientFundReport);
        return finanicalOtcClientFundReportDTO;
    }

    @Override
    public List<String> findReportNamesByType(ReportTypeEnum reportType) {
        switch (reportType) {
            case PNL:
                return pnlRepo.findAllReportName();
            case RISK:
                return riskRepo.findAllReportName();
            case PNL_HST:
                return pnlHstRepo.findAllReportName();
            case LIVE_POSITION_INFO:
                return positionRepo.findAllReportName();
            case FOF:
                return otcTradeRepo.findAllReportName();
            case FOT:
                return fodRepo.findAllReportName();
            case FOC:
                return focRepo.findAllReportName();
            case IHP:
                return hedgePnlReportRepo.findAllReportName();
            case IPS:
                return profitStatisicsReportRepo.findAllReportName();
            case ISO:
                return statisticsOfRiskReportRepo.findAllReportName();
            case ISC:
                return subjectComputingReportRepo.findAllReportName();
            case MARKET_RISK:
                return marketRiskReportRepo.findAllReportName();
            default:
                throw new IllegalStateException("类型不匹配");
        }
    }

    @Override
    public List<PnlReportDTO> findAllPnlReport() {
        return pnlRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RiskReportDTO> findAllRiskReport() {
        return riskRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PnlHstReportDTO> findAllPnlHstReport() {
        return pnlHstRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PositionReportDTO> findAllPositionReport() {
        return positionRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialOtcTradeReportDTO> findAllOtcTradeReport() {
        return otcTradeRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialOtcFundDetailReportDTO> findAllFinancialOtcFundDetailReport() {
        return fodRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinanicalOtcClientFundReportDTO> findAllFinanicalOtcClientFundReport() {
        return focRepo.findAll()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HedgePnlReportDTO> findAllHedgePnlReport() {
        return hedgePnlReportRepo.findAll().stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProfitStatisicsReportDTO> findAllProfitStatisicsReport() {
        return profitStatisicsReportRepo.findAll().stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<StatisticsOfRiskReportDTO> findAllStatisticsOfRiskReport() {
        return statisticsOfRiskReportRepo.findAll().stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<SubjectComputingReportDTO> findAllSubjectComputingReport() {
        return subjectComputingReportRepo.findAll().stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<PnlReportDTO> findPnlReportByValuationDate(LocalDate valuationDate) {
        return pnlRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PnlReportDTO> findPnlReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return pnlRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RiskReportDTO> findRiskReportByValuationDate(LocalDate valuationDate) {
        return riskRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RiskReportDTO> findRiskReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return riskRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PnlHstReportDTO> findPnlHstReportByValuationDate(LocalDate valuationDate) {
        return pnlHstRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PnlHstReportDTO> findPnlHstReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return pnlHstRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PositionReportDTO> findPositionReportByValuationDate(LocalDate valuationDate) {
        return positionRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PositionReportDTO> findPositionReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return positionRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialOtcTradeReportDTO> findOtcTradeReportByValuationDate(LocalDate valuationDate) {
        return otcTradeRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialOtcTradeReportDTO> findOtcTradeReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return otcTradeRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportByValuationDate(LocalDate valuationDate) {
        return fodRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return fodRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportByValuationDate(LocalDate valuationDate) {
        return focRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return focRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HedgePnlReportDTO> findAllHedgePnlReportByValuationDate(LocalDate valuationDate) {
        return hedgePnlReportRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HedgePnlReportDTO> findAllHedgePnlReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return hedgePnlReportRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfitStatisicsReportDTO> findAllProfitStatisicsReportByValuationDate(LocalDate valuationDate) {
        return profitStatisicsReportRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfitStatisicsReportDTO> findAllProfitStatisicsReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return profitStatisicsReportRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsOfRiskReportDTO> findAllStatisticsOfRiskReportByValuationDate(LocalDate valuationDate) {
        return statisticsOfRiskReportRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsOfRiskReportDTO> findAllStatisticsOfRiskReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return statisticsOfRiskReportRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectComputingReportDTO> findAllSubjectComputingReportByValuationDate(LocalDate valuationDate) {
        return subjectComputingReportRepo.findByValuationDate(valuationDate)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectComputingReportDTO> findAllSubjectComputingReportByValuationDateAndName(LocalDate valuationDate, String reportName) {
        return subjectComputingReportRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketRiskReportDTO> findAllMarketRiskReportByDateAndName(LocalDate valuationDate, String reportName) {
        return marketRiskReportRepo.findByValuationDateAndReportName(valuationDate, reportName)
                .stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<MarketRiskReportDTO> findAllMarketRiskReportByDate(LocalDate valuationDate) {
        return marketRiskReportRepo.findByValuationDate(valuationDate)
                .stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<MarketRiskDetailReportDTO> findAllMarketDetailRiskReportByDateAndInstrument(LocalDate valuationDate, String instrument) {
        return marketRiskDetailReportRepo.findByValuationDateAndScenarioType(valuationDate, MarketScenarioType.BASE)
                .stream()
                .filter(item -> StringUtils.isBlank(instrument) || item.getUnderlyerInstrumentId().toUpperCase().contains(instrument.trim().toUpperCase()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketRiskDetailReportDTO> findClassicScenarioMarketRiskReportByDateAndInstrument(
            LocalDate valuationDate, String instrument, DataRangeEnum reportType, String subsidiary, String partyName) {
        return marketRiskDetailReportRepo.findByValuationDateAndUnderlyerInstrumentId(valuationDate, instrument)
                .stream()
                .filter(item -> item.getScenarioType() != MarketScenarioType.BASE && (item.getReportType() == null ?
                        reportType == DataRangeEnum.MARKET : item.getReportType() == reportType))
                .filter(item -> (StringUtils.isBlank(subsidiary) || subsidiary.equals(item.getSubsidiary())) &&
                        (StringUtils.isBlank(partyName) || partyName.equals(item.getPartyName())))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubsidiaryMarketRiskReportDTO> findAllSubsidiaryMarketRiskReportByDateAndSubsidiary(
            LocalDate valuationDate, String subsidiary) {
        return subsidiaryMarketRiskReportRepo.findByValuationDate(valuationDate)
                .stream()
                .filter(item -> StringUtils.isBlank(subsidiary) || item.getSubsidiary().contains(subsidiary.trim()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CounterPartyMarketRiskReportDTO> findAllCounterPartyMarketRiskReportByDateAndPartyName(
            LocalDate valuationDate, String partyName) {
        return counterPartyMarketRiskReportRepo.findByValuationDate(valuationDate)
                .stream()
                .filter(item -> StringUtils.isBlank(partyName) || item.getPartyName().contains(partyName.trim()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketRiskBySubUnderlyerReportDTO> findAllMarketRiskSubUnderlyerReportByDateAndInstrumentAndBook(
            LocalDate valuationDate, List<String> books, String instrument) {
        return marketRiskBySubUnderlyerReportRepo.findByValuationDateAndBookNameIn(valuationDate, books)
                .stream()
                .filter(item -> StringUtils.isBlank(instrument) || item.getUnderlyerInstrumentId().toUpperCase().contains(instrument.trim().toUpperCase()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CounterPartyMarketRiskByUnderlyerReportDTO> findAllCounterPartyMarketRiskByUnderlyerReportByDateAndInstrumentAndPartyName(
            LocalDate valuationDate, String partyNamePart, String instrumentIdPart) {
        return counterPartyMarketRiskByUnderlyerReportRepo.findByValuationDate(valuationDate)
                .stream()
                .filter(item -> StringUtils.isBlank(partyNamePart) || item.getPartyName().contains(partyNamePart.trim()))
                .filter(item -> StringUtils.isBlank(instrumentIdPart) || item.getUnderlyerInstrumentId().toUpperCase().contains(instrumentIdPart.trim().toUpperCase()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinancialOtcFundDetailReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        fodRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePnlReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        pnlRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRiskReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        riskRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePnlHstReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        pnlHstRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePositionReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        positionRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOtcTradeReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        otcTradeRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanicalOtcClientFundReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        focRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEodHedgePnlReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        hedgePnlReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEodProfitStatisicsReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        profitStatisicsReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEodStatisticsOfRiskReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        statisticsOfRiskReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEodSubjectComputingReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        subjectComputingReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteMarketRiskReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        marketRiskReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteMarketRiskDetailReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        marketRiskDetailReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteSubsidiaryMarketRiskReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        subsidiaryMarketRiskReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteCounterPartyMarketRiskReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        counterPartyMarketRiskReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteCounterPartyMarketRiskByUnderlyerReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        counterPartyMarketRiskByUnderlyerReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteMarketRiskBySubUnderlyerReportByReportNameAndValuationDate(String reportName, String valuationDate) {
        marketRiskBySubUnderlyerReportRepo.deleteAllByReportNameAndValuationDate(reportName, LocalDate.parse(valuationDate));
    }

    @Override
    public void deleteSpotScenariosReportByValuationDate(String valuationDate) {
        spotScenariosReportRepo.deleteAllByValuationDate(LocalDate.parse(valuationDate));
    }

    @Override
    public List<String> findReportInstrumentListByValuationDate(LocalDate valuationDate, ReportTypeEnum reportType) {
        Set<String> dataSet = Sets.newHashSet();
        switch (reportType){
            case MARKET_RISK_DETAIL:
                dataSet = marketRiskDetailReportRepo
                        .findAllByValuationDateAndScenarioType(valuationDate, MarketScenarioType.BASE)
                        .stream()
                        .map(MarketRiskDetailReport::getUnderlyerInstrumentId)
                        .collect(Collectors.toSet());
                break;
            case SUBSIDIARY_RISK_DETAIL:
                dataSet = marketRiskBySubUnderlyerReportRepo.findAllByValuationDate(valuationDate)
                        .stream()
                        .map(MarketRiskBySubUnderlyerReport::getUnderlyerInstrumentId)
                        .collect(Collectors.toSet());
                break;
            case PARTY_RISK_DETAIL:
                dataSet = counterPartyMarketRiskByUnderlyerReportRepo.findAllByValuationDate(valuationDate)
                        .stream()
                        .map(CounterPartyMarketRiskByUnderlyerReport::getUnderlyerInstrumentId)
                        .collect(Collectors.toSet());
                break;
        }
        return dataSet.stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> findReportSubsidiaryListByValuationDate(LocalDate valuationDate, ReportTypeEnum reportType) {
        Set<String> dataSet = Sets.newHashSet();
        switch (reportType){
            case SUBSIDIARY_RISK:
                dataSet = subsidiaryMarketRiskReportRepo.findByValuationDate(valuationDate)
                        .stream()
                        .map(SubsidiaryMarketRiskReport::getSubsidiary)
                        .collect(Collectors.toSet());
                break;
            case SUBSIDIARY_RISK_DETAIL:
                dataSet = marketRiskBySubUnderlyerReportRepo.findAllByValuationDate(valuationDate)
                        .stream()
                        .map(MarketRiskBySubUnderlyerReport::getBookName)
                        .collect(Collectors.toSet());
                break;
        }
        return dataSet.stream().sorted(Collator.getInstance(Locale.CHINESE)).collect(Collectors.toList());
    }

    @Override
    public List<String> findReportCounterPartyListByValuationDate(LocalDate valuationDate, ReportTypeEnum reportType) {
        Set<String> dataSet = Sets.newHashSet();
        switch (reportType){
            case PARTY_RISK:
                dataSet = counterPartyMarketRiskReportRepo.findAllByValuationDate(valuationDate)
                        .stream()
                        .map(CounterPartyMarketRiskReport::getPartyName)
                        .collect(Collectors.toSet());
                break;
            case PARTY_RISK_DETAIL:
                dataSet = counterPartyMarketRiskByUnderlyerReportRepo.findAllByValuationDate(valuationDate)
                        .stream()
                        .map(CounterPartyMarketRiskByUnderlyerReport::getPartyName)
                        .collect(Collectors.toSet());
                break;
        }
        return dataSet.stream().sorted(Collator.getInstance(Locale.CHINESE)).collect(Collectors.toList());
    }

    @Override
    public List<SpotScenariosReportDTO> findSpotScenariosReportByDateAndTypeAndInstrument(
            DataRangeEnum reportType, String contentName, LocalDate valuationDate, String underlyer) {
        return spotScenariosReportRepo.findAllByValuationDateAndReportTypeAndContentNameAndInstrumentId(
                valuationDate, reportType, contentName, underlyer).
                stream().
                map(ReportDtoDboConvertor::transToDto).
                collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<PnlReportDTO> findPnlByReportNameAndValuationDateAndBookIdPaged(
            String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize) {
        if (CollectionUtils.isEmpty(bookIds)) {
            throw new CustomException("没有可用的交易簿信息");
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<PnlReport> pnlReports = pnlRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate, bookIds, pageable);
        List<PnlReportDTO> pnlDtoList = pnlReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(pnlDtoList, pnlReports.getTotalElements());
    }

    @Override
    public List<PnlReportDTO> findPnlReportByTemplate(PnlReportDTO pnlReportDTO, List<String> bookIds) {
        PnlReport template = new PnlReport();
        BeanUtils.copyProperties(pnlReportDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return pnlRepo.findAll(Example.of(template, exampleMatcher)).stream().
                filter(item -> bookIds.contains(item.getBookName()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<RiskReportDTO> findRiskByReportNameAndValuationDateAndBookIdPaged(String reportName,
                                                                                                  List<String> bookIds,
                                                                                                  LocalDate valuationDate,
                                                                                                  Integer page, Integer pageSize) {
        if (CollectionUtils.isEmpty(bookIds)) {
            throw new CustomException("没有可用的交易簿信息");
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<RiskReport> riskReports = riskRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate,
                bookIds, pageable);
        List<RiskReportDTO> riskDtoList = riskReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(riskDtoList, riskReports.getTotalElements());
    }

    @Override
    public List<RiskReportDTO> findRiskReportByTemplate(RiskReportDTO riskReportDTO, List<String> bookIds) {
        RiskReport template = new RiskReport();
        BeanUtils.copyProperties(riskReportDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return riskRepo.findAll(Example.of(template, exampleMatcher))
                .stream()
                .filter(item -> bookIds.contains(item.getBookName()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

    }


    @Override
    public RpcResponseListPaged<PnlHstReportDTO> findPnlHstReportNameAndValuationDateAndBookIdPaged(String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize) {

        if (CollectionUtils.isEmpty(bookIds)) {
            throw new CustomException("没有可用的交易簿信息");
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<PnlHstReport> pnlHstReports = pnlHstRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate,
                bookIds, pageable);
        List<PnlHstReportDTO> pnlHstDtoList = pnlHstReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(pnlHstDtoList, pnlHstReports.getTotalElements());
    }

    @Override
    public List<PnlHstReportDTO> findPnlHstReportByTemplate(PnlHstReportDTO pnlReportDTO, List<String> bookIds) {
        PnlHstReport template = new PnlHstReport();
        BeanUtils.copyProperties(pnlReportDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return pnlHstRepo.findAll(Example.of(template, exampleMatcher))
                .stream()
                .filter(item -> bookIds.contains(item.getBookName()))
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<HedgePnlReportDTO> findHedgePnlReportNameAndValuationDatePaged(String reportName, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<HedgePnlReport> hedgePnlReports = hedgePnlReportRepo.findByReportNameAndValuationDateIn(reportName, valuationDate, pageable);
        List<HedgePnlReportDTO> hedgePnlReportDTOList = hedgePnlReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(hedgePnlReportDTOList, hedgePnlReports.getTotalElements());
    }

    @Override
    public RpcResponseListPaged<ProfitStatisicsReportDTO> findProfitStatisicsReportNameAndValuationDatePaged(String reportName, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ProfitStatisicsReport> profitStatisicsReports = profitStatisicsReportRepo.findByReportNameAndValuationDateIn(reportName, valuationDate, pageable);
        List<ProfitStatisicsReportDTO> profitStatisicsReportDTOList = profitStatisicsReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(profitStatisicsReportDTOList, profitStatisicsReports.getTotalElements());
    }

    @Override
    public RpcResponseListPaged<StatisticsOfRiskReportDTO> findStatisticsOfRiskReportNameAndValuationDatePaged(String reportName, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<StatisticsOfRiskReport> statisticsOfRiskReports = statisticsOfRiskReportRepo.findByReportNameAndValuationDateIn(reportName, valuationDate, pageable);
        List<StatisticsOfRiskReportDTO> statisticsOfRiskReportDTOList = statisticsOfRiskReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(statisticsOfRiskReportDTOList, statisticsOfRiskReports.getTotalElements());
    }

    @Override
    public RpcResponseListPaged<SubjectComputingReportDTO> findSubjectComputingReportNameAndValuationDatePaged(String reportName, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<SubjectComputingReport> subjectComputingReports = subjectComputingReportRepo.findByReportNameAndValuationDateIn(reportName, valuationDate, pageable);
        List<SubjectComputingReportDTO> subjectComputingReportDTOList = subjectComputingReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(subjectComputingReportDTOList, subjectComputingReports.getTotalElements());
    }

    @Override
    public List<PnlHstReportDTO> findLatestPnlHstReportNameAndValuationDateAndBookId(String reportName, List<String> bookIds, LocalDate valuationDate) {

        if (CollectionUtils.isEmpty(bookIds)) {
            throw new CustomException("没有可用的交易簿信息");
        }
        List<PnlHstReport> result = pnlHstRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate, bookIds);
        if (CollectionUtils.isEmpty(result)) {
            List<PnlHstReport> tempList = pnlHstRepo.findByReportNameAndValuationDateBeforeAndBookNameInOrderByValuationDateDesc(reportName, valuationDate, bookIds);
            if (!CollectionUtils.isEmpty(tempList)) {
                LocalDate latestDate = tempList.get(0).getValuationDate();
                result = tempList.stream().filter(item -> item.getValuationDate().isEqual(latestDate)).collect(Collectors.toList());
            }
        }
        return result.stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<PositionReportDTO> findPositionReportNameAndValuationDateAndBookIdPaged(String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize) {

        if (CollectionUtils.isEmpty(bookIds)) {
            throw new CustomException("没有可用的交易簿信息");
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<PositionReport> positionReports = positionRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate,
                bookIds, pageable);
        List<PositionReportDTO> positionDtoList = positionReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(positionDtoList, positionReports.getTotalElements());
    }

    @Override
    public List<PositionReportDTO> findLatestPositionReportNameAndValuationDateAndBookId(String reportName, List<String> bookIds, LocalDate valuationDate) {

        if (CollectionUtils.isEmpty(bookIds)) {
            throw new CustomException("没有可用的交易簿信息");
        }

        List<PositionReport> result = positionRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate, bookIds);
        if (CollectionUtils.isEmpty(result)) {
            List<PositionReport> tempList = positionRepo.findByReportNameAndValuationDateBeforeAndBookNameInOrderByValuationDateDesc(reportName, valuationDate, bookIds);
            if (!CollectionUtils.isEmpty(tempList)) {
                LocalDate latestDate = tempList.get(0).getValuationDate();
                result = tempList.stream().filter(item -> item.getValuationDate().isEqual(latestDate)).collect(Collectors.toList());
            }
        }
        return result.stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public List<PositionReportDTO> findPositionReportByTemplate(PositionReportDTO templateDTO, List<String> bookIds) {
        PositionReport template = new PositionReport();
        BeanUtils.copyProperties(templateDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return positionRepo.findAll(Example.of(template, exampleMatcher)).stream().
                filter(item -> bookIds.contains(item.getBookName())).map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<FinancialOtcTradeReportDTO> findOtcTradeReportNameAndValuationDatePaged(String reportName, List<String> bookIds, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FinancialOtcTradeReport> OtcTradeReports = otcTradeRepo.findByReportNameAndValuationDateAndBookNameIn(reportName, valuationDate, bookIds, pageable);
        List<FinancialOtcTradeReportDTO> financialOtcTradeReportDTOList = OtcTradeReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(financialOtcTradeReportDTOList, OtcTradeReports.getTotalElements());
    }

    @Override
    public List<FinancialOtcTradeReportDTO> findOtcTradeReportByTemplate(FinancialOtcTradeReportDTO financialOtcTradeReportDTO,
                                                                         List<String> bookIds) {
        FinancialOtcTradeReport template = new FinancialOtcTradeReport();
        BeanUtils.copyProperties(financialOtcTradeReportDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return otcTradeRepo.findAll(Example.of(template, exampleMatcher)).stream().
                filter(item -> bookIds.contains(item.getBookName())).map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportNameAndValuationDatePaged(String reportName, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FinancialOtcFundDetailReport> financialOtcFundDetailReports = fodRepo.findByReportNameAndValuationDateIn(reportName, valuationDate, pageable);
        List<FinancialOtcFundDetailReportDTO> financialOtcFundDetailReportDTOList = financialOtcFundDetailReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(financialOtcFundDetailReportDTOList, financialOtcFundDetailReports.getTotalElements());
    }

    @Override
    public List<FinancialOtcFundDetailReportDTO> findFinancialOtcFundDetailReportByTemplate(FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO) {
        FinancialOtcFundDetailReport template = new FinancialOtcFundDetailReport();
        BeanUtils.copyProperties(financialOtcFundDetailReportDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return fodRepo.findAll(Example.of(template, exampleMatcher)).stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }

    @Override
    public RpcResponseListPaged<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportNameAndValuationDatePaged(String reportName, LocalDate valuationDate, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FinanicalOtcClientFundReport> finanicalOtcClientFundReports = focRepo.findByReportNameAndValuationDateIn(reportName, valuationDate, pageable);
        List<FinanicalOtcClientFundReportDTO> finanicalOtcClientFundReportDTOList = finanicalOtcClientFundReports.getContent()
                .stream()
                .map(ReportDtoDboConvertor::transToDto)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(finanicalOtcClientFundReportDTOList, finanicalOtcClientFundReports.getTotalElements());
    }

    @Override
    public List<FinanicalOtcClientFundReportDTO> findFinanicalOtcClientFundReportByTemplate(FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO) {
        FinanicalOtcClientFundReport template = new FinanicalOtcClientFundReport();
        BeanUtils.copyProperties(finanicalOtcClientFundReportDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return focRepo.findAll(Example.of(template, exampleMatcher)).stream().map(ReportDtoDboConvertor::transToDto).collect(Collectors.toList());
    }


    @Override
    public List<ValuationReportDTO> findValuationReportByLegalNames(List<String> legalNames) {
        List<ValuationReport> valuationReports = new ArrayList<>();

        legalNames.forEach(legalName -> {
            valuationRepo.findValuationReportsByLegalName(legalName)
                    .stream().max(Comparator.comparing(ValuationReport::getValuationDate)).ifPresent(valuationReports::add);
        });

        return valuationReports.stream()
                .map(report -> new ValuationReportDTO(report.getUuid(), report.getLegalName(), report.getValuationDate(), report.getPrice()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createOrUpdateValuationReport(ValuationReportDTO dto) {
        Optional<ValuationReport> report = valuationRepo.findByLegalNameAndValuationDate(dto.getLegalName(), dto.getValuationDate());

        if (report.isPresent()) {
            ValuationReport dbo = ReportDtoDboConvertor.convert(dto);
            dbo.setUuid(report.get().getUuid());
            valuationRepo.save(dbo);
        } else {
            valuationRepo.save(ReportDtoDboConvertor.convert(dto));
        }
    }

    @Override
    public ValuationReportDTO getValuationReport(UUID valuationReportId) {
        ValuationReport dbo = valuationRepo.findById(valuationReportId).orElseThrow(() -> new CustomException("没有相应的估值报告"));

        ValuationReportDTO dto = new ValuationReportDTO();
        BeanUtils.copyProperties(dbo, dto);
        return dto;
    }

    @Override
    public ValuationReportDTO findValuationReportByLegalNameAndValuationDate(String legalName, LocalDate valuationDate) {
        ValuationReport report = valuationRepo.findValuationReportsByLegalNameAndValuationDate(legalName, valuationDate).orElseThrow(() -> new CustomException("没有相应的估值报告"));
        return new ValuationReportDTO(report.getUuid(), report.getLegalName(), report.getValuationDate(), report.getPrice());
    }

    @Override
    public List<ValuationReportDTO> valuationReportSearch(ValuationReportDTO reportDto) {
        ValuationReport valuationReport = new ValuationReport();
        BeanUtils.copyProperties(reportDto, valuationReport);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return valuationRepo.findAll(Example.of(valuationReport, exampleMatcher))
                .stream()
                .map(report -> {
                    ValuationReportDTO valuationReportDto = new ValuationReportDTO();
                    BeanUtils.copyProperties(report, valuationReportDto);

                    PartyDTO party = partyService.getByLegalName(report.getLegalName());
                    valuationReportDto.setTradeEmail(party.getTradeEmail());
                    valuationReportDto.setMasterAgreementId(party.getMasterAgreementId());
                    return valuationReportDto;
                }).collect(Collectors.toList());
    }
}

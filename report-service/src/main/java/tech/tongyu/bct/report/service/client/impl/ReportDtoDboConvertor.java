package tech.tongyu.bct.report.service.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.client.dto.*;
import tech.tongyu.bct.report.dao.client.dbo.*;
import tech.tongyu.bct.report.dto.DataRangeEnum;
import tech.tongyu.bct.report.dto.report.MarketScenarioType;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReportDtoDboConvertor {
    public static ValuationReport convert(ValuationReportDTO dto) {
        ValuationReport dbo = new ValuationReport();

        BeanUtils.copyProperties(dto, dbo);
        return dbo;
    }

    public static PnlReportDTO transToDto(PnlReport pnlReport) {
        UUID uuid = pnlReport.getUuid();
        PnlReportDTO pnlReportDto = new PnlReportDTO();
        BeanUtils.copyProperties(pnlReport, pnlReportDto);
        pnlReportDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return pnlReportDto;
    }

    public static PnlReport transToDbo(PnlReportDTO pnlReportDto) {
        String uuid = pnlReportDto.getUuid();
        PnlReport pnlReport = new PnlReport();
        BeanUtils.copyProperties(pnlReportDto, pnlReport);
        pnlReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return pnlReport;
    }

    public static RiskReportDTO transToDto(RiskReport riskReport) {
        UUID uuid = riskReport.getUuid();
        RiskReportDTO riskReportDto = new RiskReportDTO();
        BeanUtils.copyProperties(riskReport, riskReportDto);
        riskReportDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return riskReportDto;
    }

    public static RiskReport transToDbo(RiskReportDTO riskReportDto) {
        String uuid = riskReportDto.getUuid();
        RiskReport riskReport = new RiskReport();
        BeanUtils.copyProperties(riskReportDto, riskReport);
        riskReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return riskReport;
    }

    public static PnlHstReportDTO transToDto(PnlHstReport pnlHstReport) {
        UUID uuid = pnlHstReport.getUuid();
        PnlHstReportDTO pnlHstReportDto = new PnlHstReportDTO();
        BeanUtils.copyProperties(pnlHstReport, pnlHstReportDto);
        pnlHstReportDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return pnlHstReportDto;
    }

    public static PnlHstReport transToDbo(PnlHstReportDTO pnlHstReportDto) {
        String uuid = pnlHstReportDto.getUuid();
        PnlHstReport pnlHstReport = new PnlHstReport();
        BeanUtils.copyProperties(pnlHstReportDto, pnlHstReport);
        pnlHstReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return pnlHstReport;
    }

    public static PositionReportDTO transToDto(PositionReport positionReport) {
        UUID uuid = positionReport.getUuid();
        PositionReportDTO positionReportDto = new PositionReportDTO();
        BeanUtils.copyProperties(positionReport, positionReportDto);
        positionReportDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return positionReportDto;
    }

    public static PositionReport transToDbo(PositionReportDTO positionReportDto) {
        String uuid = positionReportDto.getUuid();
        PositionReport positionReport = new PositionReport();
        BeanUtils.copyProperties(positionReportDto, positionReport);
        positionReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return positionReport;
    }

    public static FinancialOtcTradeReportDTO transToDto(FinancialOtcTradeReport FinancialOTCTradeReport) {
        UUID uuid = FinancialOTCTradeReport.getUuid();
        FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO = new FinancialOtcTradeReportDTO();
        BeanUtils.copyProperties(FinancialOTCTradeReport, FinancialOTCTradeReportDTO);
        FinancialOTCTradeReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return FinancialOTCTradeReportDTO;
    }

    public static FinancialOtcTradeReport transToDbo(FinancialOtcTradeReportDTO FinancialOTCTradeReportDTO) {
        String uuid = FinancialOTCTradeReportDTO.getUuid();
        FinancialOtcTradeReport FinancialOTCTradeReport = new FinancialOtcTradeReport();
        BeanUtils.copyProperties(FinancialOTCTradeReportDTO, FinancialOTCTradeReport);
        FinancialOTCTradeReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return FinancialOTCTradeReport;
    }

    public static FinancialOtcFundDetailReportDTO transToDto(FinancialOtcFundDetailReport financialOtcFundDetailReport) {
        UUID uuid = financialOtcFundDetailReport.getUuid();
        FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO = new FinancialOtcFundDetailReportDTO();
        BeanUtils.copyProperties(financialOtcFundDetailReport, financialOtcFundDetailReportDTO);
        financialOtcFundDetailReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return financialOtcFundDetailReportDTO;
    }

    public static FinancialOtcFundDetailReport transToDbo(FinancialOtcFundDetailReportDTO financialOtcFundDetailReportDTO) {
        String uuid = financialOtcFundDetailReportDTO.getUuid();
        FinancialOtcFundDetailReport financialOtcFundDetailReport = new FinancialOtcFundDetailReport();
        BeanUtils.copyProperties(financialOtcFundDetailReportDTO, financialOtcFundDetailReport);
        financialOtcFundDetailReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return financialOtcFundDetailReport;
    }

    public static FinanicalOtcClientFundReportDTO transToDto(FinanicalOtcClientFundReport finanicalOtcClientFundReport) {
        UUID uuid = finanicalOtcClientFundReport.getUuid();
        FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO = new FinanicalOtcClientFundReportDTO();
        BeanUtils.copyProperties(finanicalOtcClientFundReport, finanicalOtcClientFundReportDTO);
        finanicalOtcClientFundReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return finanicalOtcClientFundReportDTO;
    }

    public static FinanicalOtcClientFundReport transToDbo(FinanicalOtcClientFundReportDTO finanicalOtcClientFundReportDTO) {
        String uuid = finanicalOtcClientFundReportDTO.getUuid();
        FinanicalOtcClientFundReport finanicalOtcClientFundReport = new FinanicalOtcClientFundReport();
        BeanUtils.copyProperties(finanicalOtcClientFundReportDTO, finanicalOtcClientFundReport);
        finanicalOtcClientFundReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return finanicalOtcClientFundReport;
    }

    public static HedgePnlReportDTO transToDto(HedgePnlReport hedgePnlReport) {
        UUID uuid = hedgePnlReport.getUuid();
        HedgePnlReportDTO hedgePnlReportDTO = new HedgePnlReportDTO();
        BeanUtils.copyProperties(hedgePnlReport, hedgePnlReportDTO);
        hedgePnlReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return hedgePnlReportDTO;
    }

    public static HedgePnlReport transToDbo(HedgePnlReportDTO hedgePnlReportDTO) {
        String uuid = hedgePnlReportDTO.getUuid();
        HedgePnlReport hedgePnlReport = new HedgePnlReport();
        BeanUtils.copyProperties(hedgePnlReportDTO, hedgePnlReport);
        hedgePnlReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return hedgePnlReport;
    }

    public static ProfitStatisicsReportDTO transToDto(ProfitStatisicsReport profitStatisicsReport) {
        UUID uuid = profitStatisicsReport.getUuid();
        ProfitStatisicsReportDTO profitStatisicsReportDTO = new ProfitStatisicsReportDTO();
        BeanUtils.copyProperties(profitStatisicsReport, profitStatisicsReportDTO);
        profitStatisicsReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return profitStatisicsReportDTO;
    }

    public static ProfitStatisicsReport transToDbo(ProfitStatisicsReportDTO profitStatisicsReportDTO) {
        String uuid = profitStatisicsReportDTO.getUuid();
        ProfitStatisicsReport profitStatisicsReport = new ProfitStatisicsReport();
        BeanUtils.copyProperties(profitStatisicsReportDTO, profitStatisicsReport);
        profitStatisicsReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return profitStatisicsReport;
    }

    public static StatisticsOfRiskReportDTO transToDto(StatisticsOfRiskReport statisticsOfRiskReport) {
        UUID uuid = statisticsOfRiskReport.getUuid();
        StatisticsOfRiskReportDTO statisticsOfRiskReportDTO = new StatisticsOfRiskReportDTO();
        BeanUtils.copyProperties(statisticsOfRiskReport, statisticsOfRiskReportDTO);
        statisticsOfRiskReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return statisticsOfRiskReportDTO;
    }

    public static StatisticsOfRiskReport transToDbo(StatisticsOfRiskReportDTO intradayProfitStatisicsReportDTO) {
        String uuid = intradayProfitStatisicsReportDTO.getUuid();
        StatisticsOfRiskReport statisticsOfRiskReport = new StatisticsOfRiskReport();
        BeanUtils.copyProperties(intradayProfitStatisicsReportDTO, statisticsOfRiskReport);
        statisticsOfRiskReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return statisticsOfRiskReport;
    }

    public static SubjectComputingReportDTO transToDto(SubjectComputingReport subjectComputingReport) {
        UUID uuid = subjectComputingReport.getUuid();
        SubjectComputingReportDTO subjectComputingReportDTO = new SubjectComputingReportDTO();
        BeanUtils.copyProperties(subjectComputingReport, subjectComputingReportDTO);
        subjectComputingReportDTO.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return subjectComputingReportDTO;
    }

    public static SubjectComputingReport transToDbo(SubjectComputingReportDTO subjectComputingReportDTO) {
        String uuid = subjectComputingReportDTO.getUuid();
        SubjectComputingReport subjectComputingReport = new SubjectComputingReport();
        BeanUtils.copyProperties(subjectComputingReportDTO, subjectComputingReport);
        subjectComputingReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return subjectComputingReport;
    }

    public static MarketRiskReport transToDbo(MarketRiskReportDTO dto) {
        String uuid = dto.getUuid();
        MarketRiskReport report = new MarketRiskReport();
        BeanUtils.copyProperties(dto, report);
        report.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return report;
    }

    public static MarketRiskReportDTO transToDto(MarketRiskReport report) {
        UUID uuid = report.getUuid();
        MarketRiskReportDTO dto = new MarketRiskReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setUuid(uuid.toString());
        return dto;
    }

    public static MarketRiskDetailReport transToDbo(MarketRiskDetailReportDTO dto) {
        String uuid = dto.getUuid();
        MarketRiskDetailReport report = new MarketRiskDetailReport();
        BeanUtils.copyProperties(dto, report);
        report.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return report;
    }

    public static MarketRiskDetailReportDTO transToDto(MarketRiskDetailReport report) {
        MarketRiskDetailReportDTO dto = new MarketRiskDetailReportDTO();
        BeanUtils.copyProperties(report, dto);
        MarketScenarioType scenarioType = report.getScenarioType();
        dto.setScenarioName(scenarioType == null ? null : report.getScenarioType().getDescription());
        DataRangeEnum reportType = report.getReportType();
        dto.setReportType(reportType == null ? DataRangeEnum.MARKET : reportType);
        dto.setUuid(report.getUuid().toString());
        return dto;
    }

    public static SubsidiaryMarketRiskReport transToDbo(SubsidiaryMarketRiskReportDTO dto) {
        String uuid = dto.getUuid();
        SubsidiaryMarketRiskReport report = new SubsidiaryMarketRiskReport();
        BeanUtils.copyProperties(dto, report);
        report.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return report;
    }

    public static SubsidiaryMarketRiskReportDTO transToDto(SubsidiaryMarketRiskReport report) {
        SubsidiaryMarketRiskReportDTO dto = new SubsidiaryMarketRiskReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setUuid(report.getUuid().toString());
        return dto;
    }

    public static CounterPartyMarketRiskReport transToDbo(CounterPartyMarketRiskReportDTO dto) {
        String uuid = dto.getUuid();
        CounterPartyMarketRiskReport report = new CounterPartyMarketRiskReport();
        BeanUtils.copyProperties(dto, report);
        report.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return report;
    }

    public static CounterPartyMarketRiskReportDTO transToDto(CounterPartyMarketRiskReport report) {
        CounterPartyMarketRiskReportDTO dto = new CounterPartyMarketRiskReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setUuid(report.getUuid().toString());
        return dto;
    }

    public static MarketRiskBySubUnderlyerReportDTO transToDto(MarketRiskBySubUnderlyerReport riskReport) {
        UUID uuid = riskReport.getUuid();
        MarketRiskBySubUnderlyerReportDTO riskReportDto = new MarketRiskBySubUnderlyerReportDTO();
        BeanUtils.copyProperties(riskReport, riskReportDto);
        riskReportDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return riskReportDto;
    }

    public static MarketRiskBySubUnderlyerReport transToDbo(MarketRiskBySubUnderlyerReportDTO riskReportDto) {
        String uuid = riskReportDto.getUuid();
        MarketRiskBySubUnderlyerReport riskReport = new MarketRiskBySubUnderlyerReport();
        BeanUtils.copyProperties(riskReportDto, riskReport);
        riskReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return riskReport;
    }

    public static SpotScenariosReport transToDbo(SpotScenariosReportDTO dto) {
        String uuid = dto.getUuid();
        JsonNode scenarios = dto.getScenarios();
        SpotScenariosReport report = new SpotScenariosReport();
        BeanUtils.copyProperties(dto, report);
        report.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        report.setScenarios(scenarios);
        return report;
    }

    public static SpotScenariosReportDTO transToDto(SpotScenariosReport report) {
        UUID uuid = report.getUuid();
        SpotScenariosReportDTO dto = new SpotScenariosReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        return dto;
    }

    public static CounterPartyMarketRiskByUnderlyerReport transToDbo(CounterPartyMarketRiskByUnderlyerReportDTO dto) {
        String uuid = dto.getUuid();
        CounterPartyMarketRiskByUnderlyerReport report = new CounterPartyMarketRiskByUnderlyerReport();
        BeanUtils.copyProperties(dto, report);
        report.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        return report;
    }

    public static CounterPartyMarketRiskByUnderlyerReportDTO transToDto(CounterPartyMarketRiskByUnderlyerReport report) {
        CounterPartyMarketRiskByUnderlyerReportDTO dto = new CounterPartyMarketRiskByUnderlyerReportDTO();
        BeanUtils.copyProperties(report, dto);
        dto.setUuid(report.getUuid().toString());
        return dto;
    }
}

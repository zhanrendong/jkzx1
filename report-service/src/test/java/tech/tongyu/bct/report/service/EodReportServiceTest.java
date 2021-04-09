package tech.tongyu.bct.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.BeforeClass;
import org.junit.Test;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.AssetClassEnum;
import tech.tongyu.bct.report.dao.repo.intel.GenericEodReportRepo;
import tech.tongyu.bct.report.dto.GenericEodReportRowDTO;
import tech.tongyu.bct.report.dto.ReportTypeEnum;
import tech.tongyu.bct.report.dto.report.PositionReportRowDTO;
import tech.tongyu.bct.report.dto.report.RiskReportRowDTO;
import tech.tongyu.bct.report.mock.MockGenericEodReportRepo;
import tech.tongyu.bct.report.service.impl.EodReportServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class EodReportServiceTest {/*
    public static GenericEodReportRepo eodReportRepo = new MockGenericEodReportRepo();
    public static EodReportService reportService = new EodReportServiceImpl(eodReportRepo);
    public static String reportName = "risk";
    public static LocalDate valuationDate = LocalDate.of(2019, 1, 2);

    @BeforeClass
    public static void init() {*//*
        Map<String, Object> row1 = new HashMap<>();
        row1.put("positionId", "pos1");
        row1.put("quantity", "1");
        row1.put("underlyerInstrumentId", "abc");
        row1.put("underlyerPrice", "1.0");
        row1.put("underlyerForward", "2.0");
        row1.put("vol", " 0.2");
        row1.put("r", " 0.05");
        row1.put("q", " 3.4");
        row1.put("price", " 4.2");
        row1.put("delta", " 0.1");
        row1.put("gamma", " 0.2");
        row1.put("vega", " 0.3");
        row1.put("theta", " 0.4");
        row1.put("baseContractInstrumentId", "abc");
        row1.put("baseContractPrice", "1.0");
        row1.put("baseContractDelta", "2.0");
        row1.put("baseContractGamma", "3.0");
        row1.put("baseContractTheta", "4.0");
        System.out.println(JsonUtils.objectToJsonString(row1));
        PositionReportRowDTO p = new PositionReportRowDTO();
        p.setBookId("book1");
        p.setTradeId("t1");
        p.setPositionId("pos1");
        p.setTradeStatus(TradeStatusEnum.LIVE);
        p.setPositionStatus(LCMEventTypeEnum.OPEN);
        p.setTradeDate(LocalDate.now());
        p.setAssetClass(AssetClassEnum.EQUITY);
        p.setProductType(ProductTypeEnum.VANILLA_EUROPEAN);
        p.setUnderlyerInstrumentId("600926.SH");
        p.setUnderlyerInstrumentType("STOCK");
        p.setUnderlyerInstrumentMultiplier(1);
        p.setNotional(BigDecimal.valueOf(1000));
        p.setNumberOfLots(BigDecimal.valueOf(1));
        p.setInitialSpot(BigDecimal.valueOf(2.34));
        p.setStrike(BigDecimal.valueOf(2.4));
        p.setOptionType(OptionTypeEnum.CALL);
        p.setExpirationDate(LocalDate.now().plusMonths(3));
        p.setPayout(BigDecimal.valueOf(100));
        System.out.println(JsonUtils.objectToJsonString(p));
        Map<String, Object> row2 = new HashMap<>();
        row1.put("positionId", "pos2");
        row1.put("quantity", "1");
        row1.put("underlyerInstrumentId", "def");
        row1.put("underlyerPrice", "2.0");
        row1.put("underlyerForward", "3.0");
        row1.put("vol", " 0.2");
        row1.put("r", " 0.05");
        row1.put("q", " 3.4");
        row1.put("price", " 4.2");
        row1.put("delta", " 0.1");
        row1.put("gamma", " 0.2");
        row1.put("vega", " 0.3");
        row1.put("theta", " 0.4");
        row1.put("baseContractInstrumentId", "efs");
        row1.put("baseContractPrice", "1.0");
        row1.put("baseContractDelta", "2.0");
        row1.put("baseContractGamma", "3.0");
        row1.put("baseContractTheta", "4.0");
        reportService.createReport(reportName, ReportTypeEnum.RISK, valuationDate, "task1", Arrays.asList(row1, row2));
    *//*}

    @Test
    public void testEodReportService() throws JsonProcessingException {
        *//*List<GenericEodReportRowDTO> report =
                reportService.getReportByNameAndTypeAndDate(reportName, ReportTypeEnum.RISK, valuationDate);
        assertTrue(report.size() == 2);
        final GenericEodReportRowDTO origRow = report.get(0);
        RiskReportRowDTO rowData = JsonUtils.mapper.treeToValue(origRow.getReportData(), RiskReportRowDTO.class);
        rowData.setDelta(BigDecimal.valueOf(100.0));
        origRow.setReportData(JsonUtils.mapper.valueToTree(rowData));
        reportService.saveReportRow(origRow);
        report = reportService.getReportByNameAndTypeAndDate(reportName, ReportTypeEnum.RISK, valuationDate);
        GenericEodReportRowDTO row = report.stream().filter(r -> r.getReportUuid().equals(origRow.getReportUuid())).findAny().get();
        rowData = JsonUtils.mapper.treeToValue(row.getReportData(), RiskReportRowDTO.class);
        assertTrue(rowData.getDelta().compareTo(BigDecimal.valueOf(100.0)) == 0);
        reportService.deleteByNameAndTypeAndDate(reportName, ReportTypeEnum.RISK, valuationDate);
        List<GenericEodReportRowDTO> deleteResult =
                reportService.getReportByNameAndTypeAndDate(reportName, ReportTypeEnum.RISK, valuationDate);
        assertTrue(CollectionUtils.isEmpty(deleteResult));*//*

    }

*/
}

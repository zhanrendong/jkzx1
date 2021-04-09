package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.report.dao.client.dbo.MarketRiskBySubUnderlyerReport;
import tech.tongyu.bct.report.dao.client.dbo.RiskReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketRiskBySubUnderlyerReportRepo extends JpaRepository<MarketRiskBySubUnderlyerReport, UUID> {

    @Query("select distinct r.reportName from MarketRiskBySubUnderlyerReport r")
    List<String> findAllReportName();

    List<MarketRiskBySubUnderlyerReport> findByValuationDate(LocalDate valuationDate);

    List<MarketRiskBySubUnderlyerReport> findByValuationDateAndBookNameIn(LocalDate valuationDate, List<String> bookName);

    List<MarketRiskBySubUnderlyerReport> findByValuationDateAndReportName(LocalDate valuationDate, String reportName);

    void deleteByBookNameAndReportNameAndUnderlyerInstrumentIdAndValuationDate(String bookName, String reportName,
                                                                               String instrumentId, LocalDate valuationDate);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);

    List<MarketRiskBySubUnderlyerReport> findAllByValuationDate(LocalDate valuationDate);
}

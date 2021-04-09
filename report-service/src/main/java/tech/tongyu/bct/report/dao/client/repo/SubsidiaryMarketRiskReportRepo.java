package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.report.dao.client.dbo.SubsidiaryMarketRiskReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubsidiaryMarketRiskReportRepo extends JpaRepository<SubsidiaryMarketRiskReport, UUID> {

    List<SubsidiaryMarketRiskReport> findByValuationDate(LocalDate valuationDate);

    void deleteByValuationDateAndSubsidiary(LocalDate valuationDate, String subsidiary);

    void deleteAllByReportNameAndValuationDate(String reportName,LocalDate valuationDate);
}
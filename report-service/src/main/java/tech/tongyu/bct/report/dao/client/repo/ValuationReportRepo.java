package tech.tongyu.bct.report.dao.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.report.dao.client.dbo.ValuationReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValuationReportRepo extends JpaRepository<ValuationReport, UUID> {
    List<ValuationReport> findValuationReportsByLegalName(String legalName);

    Optional<ValuationReport> findByLegalNameAndValuationDate(String legalName, LocalDate valuationDate);

    Optional<ValuationReport> findValuationReportsByLegalNameAndValuationDate(String legalName, LocalDate valuationDate);
}

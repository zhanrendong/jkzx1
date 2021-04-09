package tech.tongyu.bct.report.mock;

import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.report.dao.dbo.GenericEodReport;
import tech.tongyu.bct.report.dao.repo.intel.GenericEodReportRepo;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MockGenericEodReportRepo extends MockJpaRepository<GenericEodReport> implements GenericEodReportRepo {
    public MockGenericEodReportRepo() {
        super(new LinkedList<>());
    }

    @Override
    public Boolean existsByReportNameAndReportTypeAndValuationDate(String reportName, ReportTypeEnum reportType, LocalDate valuationDate) {
        return data.stream().anyMatch(r -> r.getReportName().equals(reportName) &&
                r.getReportType().equals(reportType) &&
                r.getValuationDate().equals(valuationDate)
        );
    }

    @Override
    public List<GenericEodReport> findAllByReportNameAndReportTypeAndValuationDate(String reportName, ReportTypeEnum reportType, LocalDate valuationDate) {
        return data.stream().filter(r -> r.getReportName().equals(reportName) &&
                r.getReportType().equals(reportType) &&
                r.getValuationDate().equals(valuationDate)
        ).collect(Collectors.toList());
    }

    @Override
    public void deleteAllByReportNameAndReportTypeAndValuationDate(String reportName, ReportTypeEnum reportType, LocalDate valuationDate) {
        List<GenericEodReport> removeList = data.stream().filter(r -> r.getReportName().equals(reportName) &&
                r.getReportType().equals(reportType) &&
                r.getValuationDate().equals(valuationDate)
        ).collect(Collectors.toList());
        data.removeAll(removeList);
    }

    @Override
    public Boolean existsByValuationDate(LocalDate valuationDate) {
        return null;
    }

    @Override
    public List<GenericEodReport> findAllByValuationDate(LocalDate valuationDate) {
        return null;
    }
}

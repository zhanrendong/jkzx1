package tech.tongyu.bct.report.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.report.dao.dbo.GenericEodReport;

@Configuration
@ComponentScan(basePackageClasses = ReportDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = GenericEodReport.class)
public class ReportDaoConfig {
}

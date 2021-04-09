package tech.tongyu.bct.report.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.report.dao.ReportDaoConfig;

@Configuration
@Import(value = {ReportDaoConfig.class})
@ComponentScan(basePackageClasses = ReportServiceConfig.class)
public class ReportServiceConfig {

}

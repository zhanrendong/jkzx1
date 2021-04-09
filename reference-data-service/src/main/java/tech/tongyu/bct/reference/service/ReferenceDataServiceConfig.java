package tech.tongyu.bct.reference.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.reference.dao.ReferenceDataDaoConfig;

@Configuration
@Import(value = {ReferenceDataDaoConfig.class})
@ComponentScan(basePackageClasses = ReferenceDataServiceConfig.class)
public class ReferenceDataServiceConfig {
}

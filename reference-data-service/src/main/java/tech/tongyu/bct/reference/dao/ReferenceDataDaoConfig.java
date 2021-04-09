package tech.tongyu.bct.reference.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.reference.dao.dbo.TradingCalendar;

@Configuration
@ComponentScan(basePackageClasses = ReferenceDataDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = TradingCalendar.class)
public class ReferenceDataDaoConfig {
}

package tech.tongyu.bct.pricing.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.pricing.dao.dbo.CashRule;

@Configuration
@ComponentScan(basePackageClasses = PricingDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = CashRule.class)
public class PricingDaoConfig {
}

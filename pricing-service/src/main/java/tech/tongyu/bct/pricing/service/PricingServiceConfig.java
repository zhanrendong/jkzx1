package tech.tongyu.bct.pricing.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.pricing.dao.PricingDaoConfig;

@Configuration
@Import(value = {PricingDaoConfig.class})
@ComponentScan(basePackageClasses = PricingServiceConfig.class)
public class PricingServiceConfig {
}

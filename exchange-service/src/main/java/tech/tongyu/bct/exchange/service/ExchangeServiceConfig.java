package tech.tongyu.bct.exchange.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.exchange.dao.ExchangeDaoConfig;

@Configuration
@Import(ExchangeDaoConfig.class)
@ComponentScan(basePackageClasses = ExchangeServiceConfig.class)
public class ExchangeServiceConfig {
}

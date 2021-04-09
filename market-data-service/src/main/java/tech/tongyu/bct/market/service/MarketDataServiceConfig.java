package tech.tongyu.bct.market.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.market.dao.MarketDataDaoConfig;

@Configuration
@Import(MarketDataDaoConfig.class)
@ComponentScan(basePackageClasses = MarketDataServiceConfig.class)
public class MarketDataServiceConfig {
}

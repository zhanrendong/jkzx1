package tech.tongyu.bct.trade;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.trade.dao.TradeServiceDaoConfig;

@Configuration
@Import(TradeServiceDaoConfig.class)
@ComponentScan(basePackageClasses = TradeServiceConfig.class)
public class TradeServiceConfig {
}

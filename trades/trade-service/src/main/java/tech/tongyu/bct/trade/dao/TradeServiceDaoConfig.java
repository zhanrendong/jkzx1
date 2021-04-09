package tech.tongyu.bct.trade.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.trade.dao.dbo.TradeCashFlow;

@Configuration
@ComponentScan(basePackageClasses = TradeServiceDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = {TradeCashFlow.class})
public class TradeServiceDaoConfig {
}

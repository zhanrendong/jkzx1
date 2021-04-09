package tech.tongyu.bct.market.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.market.dao.dbo.Instrument;

@Configuration
@ComponentScan(basePackageClasses = MarketDataDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = {Instrument.class})
public class MarketDataDaoConfig {
}

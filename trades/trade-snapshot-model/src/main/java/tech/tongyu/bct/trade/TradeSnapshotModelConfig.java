package tech.tongyu.bct.trade;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.trade.dao.TradeSnapshotDaoConfig;

@Configuration
@Import(TradeSnapshotDaoConfig.class)
@ComponentScan(basePackageClasses = TradeSnapshotModelConfig.class)
public class TradeSnapshotModelConfig {
}

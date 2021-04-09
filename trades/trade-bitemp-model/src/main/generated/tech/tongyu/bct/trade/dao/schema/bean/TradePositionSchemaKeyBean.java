package tech.tongyu.bct.trade.dao.schema.bean;

import java.lang.Class;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchemaKey;

@Configuration
public class TradePositionSchemaKeyBean {
  @Bean(
      name = "trade_position_schema_key"
  )
  public Class<TradePositionSchemaKey> tradePositionSchemaKeyClass() {
    return TradePositionSchemaKey.class;
  }
}

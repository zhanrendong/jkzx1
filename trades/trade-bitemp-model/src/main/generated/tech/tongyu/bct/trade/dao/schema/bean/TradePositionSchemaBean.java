package tech.tongyu.bct.trade.dao.schema.bean;

import java.lang.Class;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.trade.dao.schema.model.TradePositionSchema;

@Configuration
public class TradePositionSchemaBean {
  @Bean(
      name = "trade_position_schema"
  )
  public Class<TradePositionSchema> tradePositionSchemaClass() {
    return TradePositionSchema.class;
  }
}

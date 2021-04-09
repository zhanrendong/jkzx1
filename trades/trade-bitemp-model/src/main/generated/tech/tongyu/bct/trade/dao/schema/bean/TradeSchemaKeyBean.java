package tech.tongyu.bct.trade.dao.schema.bean;

import java.lang.Class;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchemaKey;

@Configuration
public class TradeSchemaKeyBean {
  @Bean(
      name = "bct_trade_key"
  )
  public Class<TradeSchemaKey> tradeSchemaKeyClass() {
    return TradeSchemaKey.class;
  }
}

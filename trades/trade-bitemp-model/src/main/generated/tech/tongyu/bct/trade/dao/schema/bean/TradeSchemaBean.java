package tech.tongyu.bct.trade.dao.schema.bean;

import java.lang.Class;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.trade.dao.schema.model.TradeSchema;

@Configuration
public class TradeSchemaBean {
  @Bean(
      name = "bct_trade"
  )
  public Class<TradeSchema> tradeSchemaClass() {
    return TradeSchema.class;
  }
}

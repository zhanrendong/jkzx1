package tech.tongyu.bct.workflow.process.func.action.trade;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.rpc.json.http.client.RpcHttpConfig;

@Configuration
@ComponentScan(basePackageClasses = TradeImplConfig.class)
@Import(RpcHttpConfig.class)
public class TradeImplConfig {
}

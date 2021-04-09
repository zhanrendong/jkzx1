package tech.tongyu.bct.workflow.process.func.action.cap;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.rpc.json.http.client.RpcHttpConfig;

@Configuration
@ComponentScan(basePackageClasses = CapImplConfig.class)
@Import(RpcHttpConfig.class)
public class CapImplConfig {
}

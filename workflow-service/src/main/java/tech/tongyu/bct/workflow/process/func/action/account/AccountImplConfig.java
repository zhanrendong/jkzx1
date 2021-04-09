package tech.tongyu.bct.workflow.process.func.action.account;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.rpc.json.http.client.RpcHttpConfig;
/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Configuration
@ComponentScan(basePackageClasses = AccountImplConfig.class)
@Import(RpcHttpConfig.class)
public class AccountImplConfig {
}

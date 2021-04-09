package tech.tongyu.bct.auth.authaction;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.auth.manager.ManagerConfig;

@Configuration
@ComponentScan(basePackageClasses = AuthActionConfig.class)
@Import(ManagerConfig.class)
public class AuthActionConfig {
}

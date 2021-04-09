package tech.tongyu.bct.auth.business;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.auth.authaction.AuthActionConfig;
import tech.tongyu.bct.auth.manager.ManagerConfig;

@Configuration
@ComponentScan(basePackageClasses = BusinessConfig.class)
@Import({AuthActionConfig.class, ManagerConfig.class})
public class BusinessConfig {
}

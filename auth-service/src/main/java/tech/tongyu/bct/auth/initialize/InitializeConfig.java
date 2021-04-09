package tech.tongyu.bct.auth.initialize;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.auth.service.impl.ServiceConfig;

@Configuration
@ComponentScan(basePackageClasses = InitializeConfig.class)
@Import(ServiceConfig.class)
public class InitializeConfig {
}

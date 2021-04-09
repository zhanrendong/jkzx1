package tech.tongyu.bct.auth.service.impl;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import tech.tongyu.bct.auth.cache.RedisConfig;
import tech.tongyu.bct.auth.manager.ManagerConfig;

@Configuration
@ComponentScan(basePackageClasses = ServiceConfig.class)
@Import({ManagerConfig.class, RedisConfig.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ServiceConfig {

}

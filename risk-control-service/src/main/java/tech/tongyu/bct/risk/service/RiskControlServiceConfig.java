package tech.tongyu.bct.risk.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.risk.repo.RiskControlRepoConfig;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Configuration
@Import(value = {RiskControlRepoConfig.class})
@ComponentScan(basePackageClasses = RiskControlServiceConfig.class)
public class RiskControlServiceConfig {
}

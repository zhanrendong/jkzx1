package tech.tongyu.bct.risk.repo;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.risk.repo.entities.BaseEntity;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Configuration
@ComponentScan(basePackageClasses = RiskControlRepoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = BaseEntity.class)
public class RiskControlRepoConfig {
}

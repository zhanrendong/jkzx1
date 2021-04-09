package tech.tongyu.bct.workflow.process.repo;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.process.repo.entities.WorkflowEntityConfig;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Configuration
@ComponentScan(basePackageClasses = WorkflowRepoConfig.class)
@EnableJpaRepositories(basePackageClasses = WorkflowRepoConfig.class)
@EntityScan(basePackageClasses = WorkflowEntityConfig.class)
public class WorkflowRepoConfig implements DevLoadedTest {

}

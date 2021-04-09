package tech.tongyu.bct.workflow.process.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.auth.authaction.AuthActionConfig;
import tech.tongyu.bct.auth.cache.RedisConfig;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.process.service.WorkflowServiceConfig;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Configuration
@Import({AuthActionConfig.class, RedisConfig.class,WorkflowServiceConfig.class})
@ComponentScan(basePackageClasses = WorkflowApiConfig.class)
public class WorkflowApiConfig implements DevLoadedTest {
}

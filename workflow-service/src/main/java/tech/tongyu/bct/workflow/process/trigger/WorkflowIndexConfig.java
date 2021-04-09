package tech.tongyu.bct.workflow.process.trigger;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.dev.DevLoadedTest;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Configuration("WorkflowIndexConfig")
@ComponentScan(basePackageClasses = WorkflowIndexConfig.class)
public class WorkflowIndexConfig implements DevLoadedTest {
}

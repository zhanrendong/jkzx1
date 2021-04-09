package tech.tongyu.bct.workflow.process;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.process.api.WorkflowApiConfig;
import tech.tongyu.bct.workflow.process.filter.WorkflowFilterConfig;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Configuration
@ComponentScan(basePackageClasses = WorkflowProcessConfig.class)
@Import({WorkflowApiConfig.class, WorkflowFilterConfig.class})
public class WorkflowProcessConfig implements DevLoadedTest {
}

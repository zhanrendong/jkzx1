package tech.tongyu.bct.workflow.process.filter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.dev.DevLoadedTest;

@Configuration("workflowFilterConfig")
@ComponentScan(basePackageClasses = WorkflowFilterConfig.class)
public class WorkflowFilterConfig implements DevLoadedTest {
}

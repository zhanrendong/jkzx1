package tech.tongyu.bct.workflow.process.manager;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.process.repo.WorkflowRepoConfig;

@Configuration
@Import(WorkflowRepoConfig.class)
@ComponentScan(basePackageClasses = WorkflowManagerConfig.class)
public class WorkflowManagerConfig implements DevLoadedTest {
}

package tech.tongyu.bct.workflow.process.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.workflow.auth.WorkflowAuthConfig;
import tech.tongyu.bct.dev.LogUtils;
import tech.tongyu.bct.workflow.process.manager.WorkflowManagerConfig;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan(basePackageClasses = WorkflowServiceConfig.class)
@Import({WorkflowManagerConfig.class, WorkflowAuthConfig.class})
public class WorkflowServiceConfig {

    @PostConstruct
    public void started(){
        LogUtils.springLoaded(WorkflowServiceConfig.class);
    }

}

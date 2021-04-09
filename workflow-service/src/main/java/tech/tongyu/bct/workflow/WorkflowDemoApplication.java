package tech.tongyu.bct.workflow;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.rpc.json.http.server.RpcServerConfig;
import tech.tongyu.bct.workflow.auth.WorkflowAuthConfig;
import tech.tongyu.bct.workflow.initialize.WorkflowInitializeConfig;
import tech.tongyu.bct.workflow.process.WorkflowProcessConfig;

import java.util.List;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@SpringBootApplication
@Import({WorkflowProcessConfig.class, WorkflowInitializeConfig.class, WorkflowAuthConfig.class, RpcServerConfig.class})
public class WorkflowDemoApplication implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(WorkflowDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WorkflowDemoApplication.class, args);
    }

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void run(String... args) {
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().active().list();
        logger.info("> 处于激活状态的流程数量: " + processDefinitionList.size());
        for (ProcessDefinition pd : processDefinitionList) {
            logger.info("\t ===> Process definition: " + pd);
        }
    }

}

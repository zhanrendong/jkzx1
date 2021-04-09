package tech.tongyu.bct.workflow;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import tech.tongyu.bct.workflow.auth.WorkflowAuthConfig;
import tech.tongyu.bct.workflow.initialize.WorkflowInitializeConfig;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.WorkflowProcessConfig;
import tech.tongyu.bct.workflow.process.service.ProcessService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import({WorkflowProcessConfig.class, WorkflowInitializeConfig.class, WorkflowAuthConfig.class})
@Ignore
public class ProcessServiceTests {

    @Autowired
    ProcessService processService;

    @Test
    public void testGetProcessByProcessName(){
        Process process = processService.getProcessByProcessName("资金录入经办复合流程");
        System.out.println("success");
    }

    @Test
    public void modifyProcessStatus(){
        processService.modifyProcessStatus("交易录入经办复合流程",false);
        Process process = processService.getProcessByProcessName("交易录入经办复合流程");
        Assert.assertFalse(process.getStatus());
    }

}

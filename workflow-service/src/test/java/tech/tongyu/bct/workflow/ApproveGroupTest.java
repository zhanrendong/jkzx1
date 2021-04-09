package tech.tongyu.bct.workflow;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.auth.WorkflowAuthConfig;
import tech.tongyu.bct.workflow.dto.ApproveGroupDTO;
import tech.tongyu.bct.workflow.initialize.WorkflowInitializeConfig;
import tech.tongyu.bct.workflow.process.WorkflowProcessConfig;
import tech.tongyu.bct.workflow.process.service.ApproveGroupService;
import tech.tongyu.bct.workflow.process.service.TaskNodeService;

import java.util.List;
import java.util.Objects;

@RunWith(SpringRunner.class)
@Import({WorkflowProcessConfig.class, WorkflowInitializeConfig.class, WorkflowAuthConfig.class})
@SpringBootTest
@Ignore
public class ApproveGroupTest {

    @Autowired
    ApproveGroupService approveGroupService;

    @Autowired
    TaskNodeService taskNodeService;

}

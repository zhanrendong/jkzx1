package tech.tongyu.bct.workflow.process.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.TaskInstanceDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.service.TaskService;

import java.util.Collection;
import java.util.Map;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class TaskApi {

    private TaskService taskService;
    private AuthenticationService authenticationService;

    @Autowired
    public TaskApi(TaskService taskService,
        AuthenticationService authenticationService){
        this.taskService = taskService;
        this.authenticationService = authenticationService;
    }

    /**
     * 获取待办事项
     * @return 待办任务列表
     */
    @BctMethodInfo
    public Collection<TaskInstanceDTO> wkTaskInfoList(){
        return taskService.listTasksByUser(authenticationService.authenticateCurrentUser());
    }

    /**
     * 推进或回退任务
     * @param taskId 任务ID
     * @param ctlProcessData 审批数据
     * @param businessProcessData 表单数据
     * @return -> list of tasks
     */
    @BctMethodInfo
    public Collection<TaskInstanceDTO> wkTaskComplete(
            @BctMethodArg String taskId,
            @BctMethodArg Map<String, Object> ctlProcessData,
            @BctMethodArg Map<String, Object> businessProcessData){
        taskService.claimAndCompleteTask(authenticationService.authenticateCurrentUser(), taskId, CommonProcessData.of(ctlProcessData, businessProcessData));
        return taskService.listTasksByUser(authenticationService.authenticateCurrentUser());
    }
}

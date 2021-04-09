package tech.tongyu.bct.workflow.process.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.*;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceUserPerspectiveEnum;
import tech.tongyu.bct.workflow.process.service.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class ProcessInstanceApi {

    private ProcessService processService;
    private ProcessInstanceService processInstanceService;
    private AuthenticationService authenticationService;
    private RequestService requestService;
    private TriggerService triggerService;

    @Autowired
    public ProcessInstanceApi(ProcessService processService
            , AuthenticationService authenticationService
            , ProcessInstanceService processInstanceService
            , RequestService requestService
            , TriggerService triggerService){
        this.processService = processService;
        this.authenticationService = authenticationService;
        this.processInstanceService = processInstanceService;
        this.requestService = requestService;
        this.triggerService = triggerService;
    }

    /**
     * 创建流程实例
     * @param processName, 流程名称
     * @param processData, 流程数据
     * @return ProcessInstanceDTO 流程实例数据
     */
    @BctMethodInfo
    public Object wkProcessInstanceCreate(
            @BctMethodArg String processName,
            @BctMethodArg Map<String, Object> processData){
        UserDTO user = authenticationService.authenticateCurrentUser();
        Process process = processService.getProcessByProcessName(processName);
        if (!wkValidProcessCanStart(processName, processData)){
            return requestService.callService(user, processName, processData);
        }
        return processInstanceService.startProcess(
                user, process, CommonProcessData.ofBusinessProcessData(processData));
    }

    /**
     * 列出未完成的流程实例
     * @param processInstanceUserPerspective 流程实例类型: STARTED_BY_ME, EXECUTED_BY_ME
     * @return collection of process instance
     */
    @BctMethodInfo
    public Collection<ProcessInstanceDTO> wkProcessInstanceList(
            @BctMethodArg String processInstanceUserPerspective){
        UserDTO me = authenticationService.authenticateCurrentUser();
        return processInstanceService.listOngoingProcessInstance(me, ProcessInstanceUserPerspectiveEnum.of(processInstanceUserPerspective));
    }

    /**
     * 查询流程实例审批单
     * 1. 审批历史
     * 2. 审批单数据
     * 3. 审批当前节点
     * @param processInstanceId process instance's id
     * @return process instance's data
     */
    @BctMethodInfo
    public ProcessInstanceData wkProcessInstanceFormGet(
            @BctMethodArg String processInstanceId){
        UserDTO user = authenticationService.authenticateCurrentUser();
        return processInstanceService.getProcessDataByProcessInstanceId(processInstanceId, user);
    }

    /**
     * 废弃流程
     * @param processInstanceId process instance's id
     * @return collection of process instance
     */
    @BctMethodInfo
    public Collection<ProcessInstanceDTO> wkProcessInstanceTerminate(@BctMethodArg String processInstanceId){
        processInstanceService.terminateProcessInstance(processInstanceId);
        return wkProcessInstanceList(ProcessInstanceUserPerspectiveEnum.STARTED_BY_ME.toString());
    }

    @BctMethodInfo
    public Collection<ProcessInstanceDTO> wkProcessInstanceComplexList(
            @BctMethodArg String processInstanceUserPerspective,
            @BctMethodArg List<String> processInstanceStatus){
        UserDTO me = authenticationService.authenticateCurrentUser();

        Set<ProcessInstanceStatusEnum> enums = processInstanceStatus.stream()
                .map(status -> ProcessInstanceStatusEnum.of(status))
                .collect(Collectors.toSet());

        return processInstanceService.listProcessInstanceByUserPerspective(me, ProcessInstanceUserPerspectiveEnum.of(processInstanceUserPerspective), enums);
    }


    /**
     * 列出未完成的流程实例
     * @param processName 流程定义名称
     * @return collection of process instance
     */
    @BctMethodInfo
    public Collection<ProcessInstanceDTO> wkProcessInstanceListByProcessName(
            @BctMethodArg String processName){
        return processInstanceService.listOngoingProcessInstanceByProcessName(processName);
    }

    /**
     * 判断是否可发起流程
     * @param processName 流程定义名称
     * @param data 触发数据
     * @return
     */
    @BctMethodInfo
    public Boolean wkValidProcessCanStart(
            @BctMethodArg(name = "流程定义名称") String processName,
            @BctMethodArg(name = "触发数据") Map<String, Object> data){
        if (StringUtils.isBlank(processName)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        if (processService.getProcessPersistenceDTOByProcessName(processName).getStatus()) {
            return triggerService.validProcessTrigger(processName, data);
        } else {
            return false;
        }
    }
}

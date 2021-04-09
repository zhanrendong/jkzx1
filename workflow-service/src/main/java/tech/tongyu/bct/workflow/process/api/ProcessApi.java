package tech.tongyu.bct.workflow.process.api;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.workflow.dto.ModifiedTaskDTO;
import tech.tongyu.bct.workflow.dto.ProcessDTO;
import tech.tongyu.bct.workflow.process.service.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.UPDATE_TASK_NODE;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.APPROVAL_GROUP;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.PROCESS_DEFINITION_INFO;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class ProcessApi {

    private ProcessService processService;
    private ProcessInstanceService processInstanceService;
    private HistoricProcessInstanceService historicProcessInstanceService;
    private AttachmentService attachmentService;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;


    @Autowired
    public ProcessApi(ProcessService processService
            , ProcessInstanceService processInstanceService
            , HistoricProcessInstanceService historicProcessInstanceService
            , AttachmentService attachmentService
            , ResourcePermissionAuthAction resourcePermissionAuthAction){
        this.processService = processService;
        this.processInstanceService = processInstanceService;
        this.historicProcessInstanceService = historicProcessInstanceService;
        this.attachmentService = attachmentService;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
    }

    /**
     * 获取流程列表
     * @return -> list of processes
     */
    @BctMethodInfo
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<ProcessDTO> wkProcessList(){
        return processService.listAllProcess().stream()
                .map(ProcessDTO::of)
                .sorted(Comparator.comparing(ProcessDTO::getProcessName, String::compareTo))
                .collect(Collectors.toList());
    }

    /**
     * 更新流程状态
     * @return
     */
    @BctMethodInfo
    public ProcessDTO wkProcessStatusModify(
            @BctMethodArg String processName,
            @BctMethodArg Boolean status){
        processService.modifyProcessStatus(processName, status);
        return wkProcessGet(processName);
    }

    /**
     * 清空流程列表
     * @return
     */
    @BctMethodInfo
    public Collection<ProcessDTO> wkProcessAllClear(){
        processInstanceService.clearAllProcessInstance();
        historicProcessInstanceService.clearAllHistoricProcessInstance();
        attachmentService.deleteAllAttachment();
        processService.clearAllProcess();
        return wkProcessList();
    }

    /**
     * 清空流程实例
     * @return
     */
    @BctMethodInfo
    public Collection<ProcessDTO> wkProcessInstanceAllClear(){
        processInstanceService.clearAllProcessInstance();
        historicProcessInstanceService.clearAllHistoricProcessInstance();
        attachmentService.deleteAllAttachment();
        return wkProcessList();
    }

    /**
     * 获取流程
     * @return
     */
    @BctMethodInfo
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public ProcessDTO wkProcessGet(@BctMethodArg String processName){
        return ProcessDTO.of(processService.getProcessByProcessName(processName));
    }

    /**
     * 自定义流程
     * 1. 检查原有的流程定义是否有正在进行中的流程实例
     * 2. 创建新的流程图并部署
     *  2.1 持久化taskNode
     *  2.2 持久化并关联相应的filter
     *  2.3 根据taskNode生成bpmn文件并部署
     * 3. 节点关联审批组
     * @param processName
     * @param taskList
     * @return
     */
    @BctMethodInfo
    public ProcessDTO wkProcessModify(
            @BctMethodArg String processName,
            @BctMethodArg List<Map<String, Object>> taskList){

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(processName)
                        , PROCESS_DEFINITION_INFO
                        , ResourcePermissionTypeEnum.UPDATE_PROCESS_DEFINITION).get(0)){
            throw new AuthorizationException(PROCESS_DEFINITION_INFO, processName, ResourcePermissionTypeEnum.UPDATE_PROCESS_DEFINITION);
        }

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(APPROVAL_GROUP.getAlias())
                        , APPROVAL_GROUP
                        , UPDATE_TASK_NODE).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.APPROVAL_GROUP, APPROVAL_GROUP.getAlias(), ResourcePermissionTypeEnum.UPDATE_TASK_NODE);
        }

        Collection<ModifiedTaskDTO> modifiedTaskDTOList = ModifiedTaskDTO.ofList(taskList);
        return ProcessDTO.of(processService.modifyProcessTaskNode(processName, modifiedTaskDTOList));
    }

    /**
     * 清除未完成的流程实例
     * @return clear process instance
     */
    @BctMethodInfo
    public Collection<ProcessDTO> wkProcessInstanceClear(){
        processInstanceService.clearAllProcessInstance();
        return wkProcessList();
    }
}

package tech.tongyu.bct.workflow.process.service.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.*;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.ProcessConstants;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceUserPerspectiveEnum;
import tech.tongyu.bct.workflow.process.filter.ProcessStartableFilter;
import tech.tongyu.bct.workflow.process.func.TaskAction;
import tech.tongyu.bct.workflow.process.manager.*;
import tech.tongyu.bct.workflow.process.manager.HistoricTaskManger;
import tech.tongyu.bct.workflow.process.manager.ProcessManager;
import tech.tongyu.bct.workflow.process.manager.act.ActHistoricProcessInstanceManger;
import tech.tongyu.bct.workflow.process.manager.act.ActProcessManager;
import tech.tongyu.bct.workflow.process.service.ProcessInstanceService;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;
import static tech.tongyu.bct.workflow.process.utils.DiagramUtils.getHighLightedFlows;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private static Logger logger = LoggerFactory.getLogger(ProcessInstanceServiceImpl.class);
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ProcessInstanceManager processInstanceManager;
    private TaskManager taskManager;
    private HistoricTaskManger historicTaskManger;
    private ProcessManager processManager;
    private ActHistoricProcessInstanceManger actHistoricProcessInstanceManger;
    private ActProcessManager actProcessManager;
    private ProcessDiagramGenerator processDiagramGenerator;
    private TaskNodeManager taskNodeManager;
    private ApproveGroupManager approveGroupManager;

    @Autowired
    public ProcessInstanceServiceImpl(
            ProcessInstanceManager processInstanceManager
            , TaskManager taskManager
            , HistoricTaskManger historicTaskManger
            , ProcessManager processManager
            , ActProcessManager actProcessManager
            , ActHistoricProcessInstanceManger actHistoricProcessInstanceManger
            , ProcessDiagramGenerator processDiagramGenerator
            , TaskNodeManager taskNodeManager
            , ApproveGroupManager approveGroupManager){
        this.processInstanceManager = processInstanceManager;
        this.taskManager = taskManager;
        this.historicTaskManger = historicTaskManger;
        this.processManager = processManager;
        this.actProcessManager = actProcessManager;
        this.actHistoricProcessInstanceManger = actHistoricProcessInstanceManger;
        this.processDiagramGenerator = processDiagramGenerator;
        this.taskNodeManager = taskNodeManager;
        this.approveGroupManager = approveGroupManager;
    }

    @Override
    @Transactional
    public ProcessInstanceDTO startProcess(UserDTO userDTO, Process process, CommonProcessData processData) {
        Collection<ProcessStartableFilter> startableFilters = process.getProcessStartableFilters();
        if(CollectionUtils.isNotEmpty(startableFilters)){
            if(startableFilters.stream()
                    .anyMatch(filter -> !filter.canProcessStart(userDTO, process, processData))) {
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNABLE_START_PROCESS, process.getProcessName());
            }
        }

        logger.info("> 创建流程{}实例...", process.getProcessName());

        Authentication.setAuthenticatedUserId(userDTO.getUserName());
        ProcessInstance processInstance = processInstanceManager.createProcessInstance(process
                , processInstanceManager.fillCtlProcessData(userDTO, process, processData));

        logger.info("> 创建流程{}实例结束", process.getProcessName());
        Task task = taskManager.getTaskByProcessInstanceId(processInstance.getProcessInstanceId());

        tech.tongyu.bct.workflow.process.Task taskNode = process.getTaskByActTaskId(task.getTaskDefinitionKey());
        String nextTaskId = taskNodeManager.getNextCounterSignTaskIdByTaskId(taskNode.getTaskId());
        //此处仅在第一个复核为会签节点时使用
        if (!StringUtils.isBlank(nextTaskId)){
            List<String> userList = approveGroupManager.getUserListByTaskNodeId(nextTaskId);
            Map<String, Object> assigneeMap = new HashMap<>();
            assigneeMap.put("AssigneeDTO", new AssigneeDTO(userList, userList.size(), 1));
            assigneeMap.put("up",0);
            assigneeMap.put("down",0);
            taskManager.claimAndCompleteHasCounterSignTask(userDTO, task, processData, assigneeMap);
        } else {
            taskManager.claimAndCompleteTask(userDTO, task, processData);
        }


        return new ProcessInstanceDTO(
                processInstance.getProcessInstanceId()
                , process.getProcessName()
                , userDTO
                , formatter.format(new Date())
                , processData.getProcessSequenceNum()
                , processData.getSubject()
                , ProcessInstanceStatusEnum.PROCESS_UNFINISHED
        );
    }

    @Override
    public void terminateProcessInstance(String processInstanceId) {
        //查询流程是否存在
        processInstanceManager.getProcessInstanceByProcessInstanceId(processInstanceId);
        //存在则发起停止
        processInstanceManager.terminateProcessInstanceByProcessInstanceId(processInstanceId);
    }

    @Override
    public List<ProcessInstanceDTO> listOngoingProcessInstance(UserDTO userDTO, ProcessInstanceUserPerspectiveEnum processInstanceUserPerspectiveEnum) {
        switch (processInstanceUserPerspectiveEnum){
            case STARTED_BY_ME:
                return processInstanceManager.listProcessInstanceDTOByStarter(userDTO.getUserName());
            case EXECUTED_BY_ME:
                return processInstanceManager.listProcessInstanceDTOByInvolvedUser(userDTO.getUserName())
                        .stream().filter(processInstanceDto -> !Objects.equals(processInstanceDto.getInitiator().getUserName(), userDTO.getUserName()))
                        .collect(Collectors.toList());
            default:
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_PROCESS_INSTANCE_USER_PERSPECTIVE, processInstanceUserPerspectiveEnum.toString());
        }
    }

    @Override
    public List<ProcessInstanceDTO> listProcessInstanceByUserPerspective(UserDTO userDTO, ProcessInstanceUserPerspectiveEnum processInstanceUserPerspectiveEnum, Collection<ProcessInstanceStatusEnum> processInstanceStatus) {
        switch (processInstanceUserPerspectiveEnum){
            case STARTED_BY_ME:
                return processInstanceManager.listProcessInstanceByStarterAndKeyword(userDTO, processInstanceStatus);
            case EXECUTED_BY_ME:
                return processInstanceManager.listProcessInstanceByKeyword(userDTO, processInstanceStatus);
            default:
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_PROCESS_INSTANCE_USER_PERSPECTIVE, processInstanceUserPerspectiveEnum.toString());
        }
    }

    @Override
    public ProcessInstanceData getProcessDataByProcessInstanceId(String processInstanceId, UserDTO userDTO) {
        ProcessInstanceData processInstanceData = new ProcessInstanceData();

        ProcessInstance processInstance = processInstanceManager.getProcessInstanceByProcessInstanceId(processInstanceId);
        CommonProcessData processData = CommonProcessData.fromActStorage(processInstance.getProcessVariables());
        processInstanceData.setProcessData(processData);
        processInstanceData.setProcessInstanceDTO(processInstanceManager.getProcessInstanceDTOByProcessInstance(processInstance));

        List<HistoricTaskInstance> historicTasks = historicTaskManger.listHistoricTaskInstanceByProcessInstanceId(processInstanceId);

        Map<String, CommonProcessData> historicVariableInstanceMap = historicTaskManger.getHistoricCommonProcessDataByProcessInstanceId(processInstanceId);

        List<TaskHistoryDTO> historyTaskDTOS = getTaskHistory(historicTasks, historicVariableInstanceMap);

        processInstanceData.setTaskHistoryDTOList(historyTaskDTOS);

        Task task = taskManager.getTaskByProcessInstanceId(processInstanceId, userDTO);
        CurrentNodeDTO currentNodeDTO = new CurrentNodeDTO(
                tech.tongyu.bct.workflow.process.Task.getTaskNameFromActTaskId(task.getTaskDefinitionKey())
                , tech.tongyu.bct.workflow.process.Task.getTaskTypeFromActTaskId(task.getTaskDefinitionKey()));

        processInstanceData.setCurrentNodeDTO(currentNodeDTO);

        return processInstanceData;
    }

    @Override
    public byte[] getProcessInstanceImageBytes(String processInstanceId) {
        //获取流程实例
        HistoricProcessInstance historicProcessInstance = actHistoricProcessInstanceManger.getHistoricByProcessInstanceId(processInstanceId);
        if (Objects.isNull(historicProcessInstance)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processInstanceId);
        }

        // 根据流程对象获取流程对象模型
        BpmnModel bpmnModel = actProcessManager.getBpmnModelByProcessDefinitionId(historicProcessInstance.getProcessDefinitionId());

        // 获取流程中已经执行的节点，按照执行先后顺序排序
        List<HistoricActivityInstance> historicActivityInstances = actHistoricProcessInstanceManger.getHistoricActivityNode(processInstanceId);
        if (Objects.isNull(historicActivityInstances)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.HISTORY_NODE_INFO, processInstanceId);
        }

        // 高亮已经执行流程节点ID集合
        List<String> highLightedActivityIds = historicActivityInstances.stream().map(HistoricActivityInstance::getActivityId).collect(Collectors.toList());

        List<String> flowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);
        try (InputStream imageStream =
                     processDiagramGenerator.generateDiagram(bpmnModel, highLightedActivityIds, flowIds,
                             "宋体", "宋体", "宋体", true, "png")){
            return IOUtils.toByteArray(imageStream);
        } catch (Exception e) {
            throw new RuntimeException("生成流程图异常！", e);
        }
    }

    @Override
    public Collection<ProcessInstanceDTO> listOngoingProcessInstanceByProcessName(String processName) {
        return processInstanceManager.listProcessInstanceDTOByProcessName(processName);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllProcessInstance() {
        processInstanceManager.clearAllProcessInstance();
    }

    public static List<TaskHistoryDTO> getTaskHistory(List<HistoricTaskInstance> historicTasks,
                                               Map<String, CommonProcessData> historicVariableInstanceMap){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return historicTasks.stream()
                .map(historicTaskInstance -> {
                    CommonProcessData data = historicVariableInstanceMap.get(historicTaskInstance.getId());

                    return new TaskHistoryDTO(
                            historicTaskInstance.getName()
                            , historicTaskInstance.getAssignee()
                            , getOperation(data)
                            , format.format(historicTaskInstance.getEndTime())
                            , Objects.isNull(data.getCtlProcessData().get(COMMENT))
                            ? ""
                            : data.getCtlProcessData().get(COMMENT).toString()
                    );
                })
                .sorted(Comparator.comparing(TaskHistoryDTO::getOperateTime, String::compareTo).reversed())
                .collect(Collectors.toList());
    }

    private static String getOperation(CommonProcessData data){
        if (data.getCtlProcessData().containsKey(TASK_HISTORY)){
            return data.getCtlProcessData().get(TASK_HISTORY).toString();
        } else {
            if (data.getCtlProcessData().containsKey(ABANDON)
                    && !((Boolean) data.getCtlProcessData().get(ABANDON))) {
                return "修改";
            } else if (data.getCtlProcessData().containsKey(CONFIRMED)
                    && ((Boolean) data.getCtlProcessData().get(CONFIRMED))) {
                return "复核通过";
            } else if (data.getCtlProcessData().containsKey(ABANDON)
                    && (Boolean) data.getCtlProcessData().get(ABANDON)) {
                return "废弃";
            } else if(data.getCtlProcessData().containsKey(CONFIRMED)
                    && !((Boolean) data.getCtlProcessData().get(CONFIRMED))) {
                return "退回";
            } else {
                return "录入";
            }
        }
    }

}

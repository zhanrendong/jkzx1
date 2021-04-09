package tech.tongyu.bct.workflow.process.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.AssigneeDTO;
import tech.tongyu.bct.workflow.dto.TaskInstanceDTO;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.TaskCompletableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;
import tech.tongyu.bct.workflow.process.func.TaskAction;
import tech.tongyu.bct.workflow.process.manager.*;
import tech.tongyu.bct.workflow.process.service.TaskService;

import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class TaskServiceImpl implements TaskService {

    private static Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private TaskManager taskManager;
    private ProcessManager processManager;
    private ProcessInstanceManager processInstanceManager;
    private ApproveGroupManager approveGroupManager;
    private TaskNodeManager taskNodeManager;

    @Autowired
    public TaskServiceImpl(TaskManager taskManager
            , ProcessManager processManager
            , ProcessInstanceManager processInstanceManager
            , ApproveGroupManager approveGroupManager
            , TaskNodeManager taskNodeManager){
        this.taskManager = taskManager;
        this.processManager = processManager;
        this.processInstanceManager = processInstanceManager;
        this.approveGroupManager = approveGroupManager;
        this.taskNodeManager = taskNodeManager;
    }

    @Override
    public Collection<TaskInstanceDTO> listTasksByUser(UserDTO userDTO) {
        if (!approveGroupManager.hasGroupByUsername(userDTO.getUserName())){
            return Lists.newArrayList();
        }
        Collection<Task> tasks = taskManager.listTasksByCandidateUser(userDTO);
        return tasks.stream()
                .map(task -> {
                    Process process = processManager.getProcessByProcessDefinitionId(task.getProcessDefinitionId());
                    tech.tongyu.bct.workflow.process.Task taskNode = process.getTaskByActTaskId(task.getTaskDefinitionKey());
                    Collection<TaskReadableFilter> readableFilters = Sets.newHashSet();
                    Collection<TaskReadableFilter> taskReadableFilters = process.getTaskTypeReadableFilters().get(taskNode.getTaskType());
                    if (!CollectionUtils.isEmpty(taskReadableFilters)){
                        readableFilters.addAll(taskReadableFilters);
                    }
                    readableFilters.addAll(taskNode.getTaskReadableFilters());
                    if(!TaskManager.filterReadableTask(readableFilters, userDTO, task)) {
                        return null;
                    }

                    TaskInstanceDTO taskInstanceDTO = new TaskInstanceDTO();
                    taskInstanceDTO.setTaskId(task.getId());
                    taskInstanceDTO.setTaskName(task.getName());
                    taskInstanceDTO.setProcessInstanceDTO(processInstanceManager.getProcessInstanceDTOByProcessInstanceId(task.getProcessInstanceId()));
                    return taskInstanceDTO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public String claimAndCompleteTask(UserDTO userDTO, String taskId, CommonProcessData processData) {
        Task task = taskManager.getTaskByTaskId(taskId);

        ProcessInstance processInstance = processInstanceManager.getProcessInstanceByTask(task);
        Process process = processManager.getProcessByProcessDefinitionId(processInstance.getProcessDefinitionId());
        tech.tongyu.bct.workflow.process.Task taskNode = process.getTaskByActTaskId(task.getTaskDefinitionKey());
        Collection<TaskCompletableFilter> completableFilters = Sets.newHashSet();
        Collection<TaskCompletableFilter> taskCompletableFilters = process.getTaskTypeCompletableFilters().get(taskNode.getTaskType());
        if (!CollectionUtils.isEmpty(taskCompletableFilters)){
            completableFilters.addAll(taskCompletableFilters);
        }
        completableFilters.addAll(taskNode.getTaskCompletableFilters());

        if(CollectionUtils.isEmpty(completableFilters)
                || completableFilters.stream()
                .allMatch(taskCompletableFilter -> taskCompletableFilter.canTaskComplete(userDTO, task, processData))){

            CommonProcessData elderProcessData = CommonProcessData.fromActStorage(processInstance.getProcessVariables());
            TaskAction taskAction = taskNode.getTaskAction();

            Authentication.setAuthenticatedUserId(userDTO.getUserName());
            logger.info("\t==> {}完成任务{}", userDTO.getUserName(), task.getName());
            if(Objects.isNull(taskAction)) {
                taskManager.claimAndCompleteTask(userDTO, task);
            } else if (process.hasCounterSignTask()) {
                if (Objects.equals(taskNode.getTaskType(),TaskTypeEnum.COUNTER_SIGN_DATA)) {
                    List<String> userList = approveGroupManager.getUserListByTaskNodeId(taskNode.getTaskId());
                    processData.put(ACT_LIST_NAME, userList);
                    CommonProcessData commonProcessData = taskAction.execute(process
                            , process.getTaskByActTaskId(task.getTaskDefinitionKey())
                            , userDTO, elderProcessData, processData, processInstance.getStartUserId());
                    Map<String, Object> assigneeMap = new HashMap<>();
                    // todo ： 通过配置AssignessDTO 进行百分比/指定用户
                    assigneeMap.put(ACT_MUIT_DTO, new AssigneeDTO(userList, userList.size(), 1));
                    taskManager.claimAndCompleteHasCounterSignTask(userDTO, task, commonProcessData, assigneeMap);
                } else if(!StringUtils.isBlank(taskNodeManager.getNextCounterSignTaskIdByTaskId(taskNode.getTaskId()))) {
                    CommonProcessData commonProcessData = taskAction.execute(process
                            , process.getTaskByActTaskId(task.getTaskDefinitionKey())
                            , userDTO, elderProcessData, processData, processInstance.getStartUserId());
                    //下一节点为会签节点,需要重置数值
                    commonProcessData.put(UP,0);
                    commonProcessData.put(DOWN,0);
                    List<String> userList = approveGroupManager.getUserListByTaskNodeId(taskNode.getTaskId());
                    Map<String, Object> assigneeMap = new HashMap<>();
                    assigneeMap.put(ACT_MUIT_DTO, new AssigneeDTO(userList, userList.size(), 1));
                    taskManager.claimAndCompleteHasCounterSignTask(userDTO, task, commonProcessData, assigneeMap);
                } else {
                    CommonProcessData commonProcessData = taskAction.execute(process
                            , process.getTaskByActTaskId(task.getTaskDefinitionKey())
                            , userDTO, elderProcessData, processData, processInstance.getStartUserId());
                    taskManager.claimAndCompleteTask(userDTO, task, commonProcessData);
                }
            } else {
                CommonProcessData commonProcessData = taskAction.execute(process
                        , process.getTaskByActTaskId(task.getTaskDefinitionKey())
                        , userDTO, elderProcessData, processData, processInstance.getStartUserId());
                taskManager.claimAndCompleteTask(userDTO, task, commonProcessData);
            }
        }

        return processInstance.getId();
    }
}

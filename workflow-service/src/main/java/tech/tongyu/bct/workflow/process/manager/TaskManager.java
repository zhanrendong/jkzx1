package tech.tongyu.bct.workflow.process.manager;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.AssigneeDTO;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TaskManager {

    private static Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private TaskService taskService;

    @Autowired
    public TaskManager(TaskService taskService){
        this.taskService = taskService;
    }

    public Task getTaskByTaskId(String taskId){
        return checkSingleTask(
                taskService.createTaskQuery()
                .taskId(taskId)
                .active()
                .list()
        );
    }

    public Task getTaskByProcessInstanceId(String processInstanceId){
        return checkSingleTask(
                taskService.createTaskQuery()
                        .processInstanceId(processInstanceId)
                        .active()
                        .list()
        );
    }

    public Task getTaskByProcessInstanceId(String processInstanceId, UserDTO userDTO){
        return checkSingleTask(
                taskService.createTaskQuery()
                        .processInstanceId(processInstanceId)
                        .active()
                        .list(), userDTO
        );
    }

    public Collection<Task> getTaskByProcessName(String processName){
        return taskService.createTaskQuery()
                .processDefinitionName(processName)
                .active()
                .list();
    }

    private Task checkSingleTask(Collection<Task> taskCollection){
        if(CollectionUtils.isEmpty(taskCollection)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_NOT_FOUND);
        }

        if(taskCollection.size() > 1) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MULTI_TASKS);
        }

        return (Task) taskCollection.toArray()[0];
    }

    private Task checkSingleTask(Collection<Task> taskCollection, UserDTO userDTO){
        if(CollectionUtils.isEmpty(taskCollection)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TASK_NOT_FOUND);
        }


        if(taskCollection.size() > 1) {
            List<Task> tasks = taskCollection
                    .stream()
                    .filter(task -> Objects.equals(task.getAssignee(), userDTO.getUserName()))
                    .collect(Collectors.toList());
            return tasks.get(0);
        }

        return (Task) taskCollection.toArray()[0];
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimAndCompleteTask(UserDTO userDTO, Task task, CommonProcessData processData){
        logger.info("> 用户{}申领待办事项: {}", userDTO.getUserName(), task.getName());
        taskService.claim(task.getId(), userDTO.getUserName());
        logger.info("> 完成待办事项: {}", task.getName());
        // taskService.complete(taskId, processVariables) 在已经存在processInstance.processVariables时
        // 新的variables不会替代，必须强制使用setVariables方法
        taskService.setVariables(task.getId(), processData.toActStorage());
        taskService.complete(task.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimAndCompleteTask(UserDTO userDTO, Task task){
        logger.info("> 用户{}申领初始待办事项: {}", userDTO.getUserName(), task.getName());
        taskService.claim(task.getId(), userDTO.getUserName());
        logger.info("> 完成初始待办事项: {}", task.getName());
        taskService.complete(task.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimAndCompleteHasCounterSignTask(UserDTO userDTO, Task task, CommonProcessData processData, Map<String, Object> assigneeMap){
        logger.info("> 用户{}申领待办事项: {}", userDTO.getUserName(), task.getName());
        taskService.claim(task.getId(), userDTO.getUserName());
        logger.info("> 完成待办事项: {}", task.getName());
        // taskService.complete(taskId, processVariables) 在已经存在processInstance.processVariables时
        // 新的variables不会替代，必须强制使用setVariables方法
        taskService.removeVariable(task.getId(), "AssigneeDTO");
        taskService.setVariables(task.getId(), processData.toActStorage());
        taskService.complete(task.getId(), assigneeMap);
    }


    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<Task> listTasksByCandidateUser(UserDTO userDTO){
        return taskService.createTaskQuery()
                .active()
                .taskCandidateOrAssigned(userDTO.getUserName())
                .list();
    }

    public static Boolean filterReadableTask(Collection<TaskReadableFilter> taskReadableFilters, UserDTO userDTO, Task task){
        return CollectionUtils.isEmpty(taskReadableFilters)
                || taskReadableFilters.stream()
                .allMatch(taskReadableFilter -> taskReadableFilter.canTaskRead(userDTO, task));
    }

}

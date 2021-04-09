package tech.tongyu.bct.workflow.dto;

import com.google.common.collect.Sets;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.process.Task;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.TaskCompletableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;
import tech.tongyu.bct.workflow.process.func.TaskAction;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class TaskDTO {

    private String taskId;
    private String taskName;
    private TaskTypeEnum taskType;
    private String taskAction;
    private Collection<String> taskReadableFilters;
    private Collection<String> taskCompletableFilters;
    private Integer sequence;
    private Collection<ApproveGroupDTO> approveGroups;

    public static Collection<TaskDTO> ofCollection(Collection<Task> tasks){
        if(CollectionUtils.isEmpty(tasks)){
            return Sets.newHashSet();
        }
        return tasks.stream()
                .map(TaskDTO::of)
                .collect(Collectors.toSet());
    }

    public static TaskDTO of(Task task){
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(task.getTaskId());
        taskDTO.setTaskName(task.getTaskName());
        taskDTO.setTaskType(task.getTaskType());
        taskDTO.setApproveGroups(task.getApproveGroups());
        TaskAction taskAction = task.getTaskAction();
        if(Objects.nonNull(taskAction)) {
            taskDTO.setTaskAction(taskAction.getClass().getName());
        }
        Collection<TaskReadableFilter> taskReadableFilters = task.getTaskReadableFilters();
        taskDTO.setTaskReadableFilters(
                taskReadableFilters.stream()
                .map(filter -> filter.getClass().getName())
                .collect(Collectors.toSet())
        );

        Collection<TaskCompletableFilter> taskCompletableFilters = task.getTaskCompletableFilters();
        taskDTO.setTaskCompletableFilters(
                taskCompletableFilters.stream()
                .map(filter -> filter.getClass().getName())
                .collect(Collectors.toSet())
        );

        taskDTO.setSequence(task.getSequence());

        return taskDTO;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskTypeEnum getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }

    public String getTaskAction() {
        return taskAction;
    }

    public void setTaskAction(String taskAction) {
        this.taskAction = taskAction;
    }

    public Collection<String> getTaskReadableFilters() {
        return taskReadableFilters;
    }

    public void setTaskReadableFilters(Collection<String> taskReadableFilters) {
        this.taskReadableFilters = taskReadableFilters;
    }

    public Collection<String> getTaskCompletableFilters() {
        return taskCompletableFilters;
    }

    public void setTaskCompletableFilters(Collection<String> taskCompletableFilters) {
        this.taskCompletableFilters = taskCompletableFilters;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Collection<ApproveGroupDTO> getApproveGroups() {
        return approveGroups;
    }

    public void setApproveGroups(Collection<ApproveGroupDTO> approveGroups) {
        this.approveGroups = approveGroups;
    }
}

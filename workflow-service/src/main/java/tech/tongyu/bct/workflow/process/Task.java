package tech.tongyu.bct.workflow.process;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.dto.ApproveGroupDTO;
import tech.tongyu.bct.workflow.dto.UserApproveGroupDTO;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.filter.TaskCompletableFilter;
import tech.tongyu.bct.workflow.process.filter.TaskReadableFilter;
import tech.tongyu.bct.workflow.process.func.TaskAction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author david.yang
 *  - mailto: yangywiei@tongyu.tech
 */
public class Task {

    private String taskId;
    private String taskName;
    private TaskTypeEnum taskType;
    private TaskAction taskAction;
    private Collection<TaskReadableFilter> taskReadableFilters;
    private Collection<TaskCompletableFilter> taskCompletableFilters;
    private Collection<ApproveGroupDTO> approveGroups;
    private Integer sequence;

    public String getActTaskId(){
        switch (taskType){
            case INPUT_DATA:
                return taskName + "_-1";
            case MODIFY_DATA:
                return taskName + "_-2";
            case REVIEW_DATA:
                return taskName + "_" + sequence;
            case COUNTER_SIGN_DATA:
                return taskName + "_" + sequence;
            default:
                throw new UnsupportedOperationException("unknown taskType");
        }
    }

    public static TaskTypeEnum getTaskTypeFromActTaskId(String actTaskId){
        String suffix = actTaskId.substring(actTaskId.lastIndexOf("_")+1);
        Integer typeNum = Integer.valueOf(suffix);
        if(Objects.equals(-1, typeNum)) {
            return TaskTypeEnum.INPUT_DATA;
        } else if(Objects.equals(-2, typeNum)) {
            return TaskTypeEnum.MODIFY_DATA;
        } else {
            return TaskTypeEnum.REVIEW_DATA;
        }
    }

    public static String getTaskNameFromActTaskId(String actTaskId){
        return actTaskId.substring(0, actTaskId.lastIndexOf("_"));
    }

    public static Integer getSequenceFromActTaskId(String actTaskId){
        String suffix = actTaskId.substring(actTaskId.lastIndexOf("_")+1);
        return Integer.valueOf(suffix);
    }

    public String getCandidateGroup(String processName){
        assert StringUtils.isNotBlank(processName);
        return processName + "_" + getActTaskId();
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

    public TaskAction getTaskAction() {
        return taskAction;
    }

    public void setTaskAction(TaskAction taskAction) {
        this.taskAction = taskAction;
    }

    public Collection<TaskReadableFilter> getTaskReadableFilters() {
        return taskReadableFilters;
    }

    public void setTaskReadableFilters(Collection<TaskReadableFilter> taskReadableFilters) {
        this.taskReadableFilters = taskReadableFilters;
    }

    public Collection<TaskCompletableFilter> getTaskCompletableFilters() {
        return taskCompletableFilters;
    }

    public void setTaskCompletableFilters(Collection<TaskCompletableFilter> taskCompletableFilters) {
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

    public Boolean isInTaskApproveGroup(String username){
        return this.getApproveGroups().stream()
                .map(ApproveGroupDTO::getUserList)
                .flatMap(Collection::stream)
                .map(UserApproveGroupDTO::getUsername)
                .distinct()
                .anyMatch(user -> Objects.equals(username, user));
    }
}

package tech.tongyu.bct.workflow.dto;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.IntegerUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.func.TaskAction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class ModifiedTaskDTO implements TaskNode{

    private ModifiedTaskDTO(){}

    public static Collection<ModifiedTaskDTO> ofList(List<Map<String, Object>> taskList){
        if(CollectionUtils.isEmpty(taskList)){
            return Sets.newHashSet();
        }
        return taskList.stream()
                .map(ModifiedTaskDTO::of)
                .collect(Collectors.toSet());
    }

    public static ModifiedTaskDTO of(Map<String, Object> map){
        ModifiedTaskDTO modifiedTaskDTO = new ModifiedTaskDTO();
        Object obTaskName = map.get(TASK_NAME);
        if(Objects.isNull(obTaskName)
            || !(obTaskName instanceof String)
            || StringUtils.isBlank(obTaskName.toString())){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_TASK_NODE);
        }

        String taskName = obTaskName.toString();

        Object obTaskType = map.get(TASK_TYPE);
        if(Objects.isNull(obTaskType)
                || !(obTaskType instanceof String)
                || StringUtils.isBlank(obTaskType.toString())){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_TASK_NODE);
        }
        Integer sequence;
        switch (TaskTypeEnum.of(obTaskType.toString())){
            case INPUT_DATA:
                sequence = -1;
                break;
            case MODIFY_DATA:
                sequence = -2;
                break;
            case REVIEW_DATA:
                sequence = IntegerUtils.num2Integer(map.get(SEQUENCE));
                break;
            case COUNTER_SIGN_DATA:
                sequence = IntegerUtils.num2Integer(map.get(SEQUENCE));
                break;
            default:
                sequence = -1;
        }

        Object obActionClass = map.get(ACTION_CLASS);
        if(Objects.nonNull(obActionClass)
            && obActionClass instanceof String
            && StringUtils.isNotBlank(obActionClass.toString())){
            try{
                Class clazz = Class.forName(obActionClass.toString());
                if(!TaskAction.class.isAssignableFrom(clazz)){
                    throw new RuntimeException();
                }
            } catch (Exception e){
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_TASK_NODE);
            }
        }
        modifiedTaskDTO.setTaskName(taskName);
        modifiedTaskDTO.setSequence(sequence);
        modifiedTaskDTO.setTaskType(TaskTypeEnum.of(obTaskType.toString()));
        modifiedTaskDTO.setActionClass(obActionClass.toString());
        modifiedTaskDTO.setApproveGroupIdList((Collection<String>) map.get(APPROVE_GROUP_LIST));

        return modifiedTaskDTO;
    }

    private String taskName;
    private Integer sequence;
    private TaskTypeEnum taskType;
    private String actionClass;
    private Collection<String> approveGroupIdList;

    @Override
    public Collection<String> getApproveGroupList() {
        return approveGroupIdList;
    }

    public Collection<String> getApproveGroupIdList() {
        return approveGroupIdList;
    }

    public void setApproveGroupIdList(Collection<String> approveGroupIdList) {
        this.approveGroupIdList = approveGroupIdList;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Override
    public TaskTypeEnum getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }

    @Override
    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }
}

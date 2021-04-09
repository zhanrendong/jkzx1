package tech.tongyu.bct.workflow.dto;

import com.google.common.collect.Sets;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.process.ProcessConstants.APPROVE_GROUP_LIST;
import static tech.tongyu.bct.workflow.process.ProcessConstants.TASK_ID;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class TaskApproveGroupDTO {

    private String taskId;
    private Collection<String> approveGroupIdList;

    public static Collection<TaskApproveGroupDTO> ofList(Collection<Map<String, Object>> maps){
        if(CollectionUtils.isEmpty(maps)){
            return Sets.newHashSet();
        }
        return maps.stream()
                .map(TaskApproveGroupDTO::of)
                .collect(Collectors.toSet());
    }

    public static TaskApproveGroupDTO of(Map<String, Object> map){
        TaskApproveGroupDTO taskApproveGroupDTO = new TaskApproveGroupDTO();
        taskApproveGroupDTO.setTaskId((String) map.get(TASK_ID));
        taskApproveGroupDTO.setApproveGroupIdList((Collection<String>) map.get(APPROVE_GROUP_LIST));

        return taskApproveGroupDTO;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Collection<String> getApproveGroupIdList() {
        return approveGroupIdList;
    }

    public void setApproveGroupIdList(Collection<String> approveGroupIdList) {
        this.approveGroupIdList = approveGroupIdList;
    }
}

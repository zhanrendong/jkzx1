package tech.tongyu.bct.workflow.process.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.workflow.dto.ApproveGroupDTO;
import tech.tongyu.bct.workflow.dto.ProcessDTO;
import tech.tongyu.bct.workflow.dto.UserApproveGroupDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.service.ApproveGroupService;
import tech.tongyu.bct.workflow.process.service.ProcessService;
import tech.tongyu.bct.workflow.process.service.TaskNodeService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.*;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.APPROVAL_GROUP;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.APPROVAL_GROUP_INFO;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ApproveGroupApi {

    private ApproveGroupService approveGroupService;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private TaskNodeService taskNodeService;
    private ProcessService processService;

    @Autowired
    public ApproveGroupApi(
            ApproveGroupService approveGroupService
            , ResourcePermissionAuthAction resourcePermissionAuthAction
            , TaskNodeService taskNodeService
            , ProcessService processService) {
        this.approveGroupService = approveGroupService;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
        this.taskNodeService = taskNodeService;
        this.processService = processService;
    }

    /**
     * create approve group
     * @param approveGroupName name of approve group
     * @param description description
     * @return list of approve group
     */
    @BctMethodInfo
    public Collection<ApproveGroupDTO> wkApproveGroupCreate(
            @BctMethodArg String approveGroupName,
            @BctMethodArg(required = false) String description){
        if (StringUtils.isBlank(approveGroupName)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND, approveGroupName);
        }
        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(APPROVAL_GROUP.getAlias())
                        , APPROVAL_GROUP
                        , CREATE_APPROVAL_GROUP).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.APPROVAL_GROUP, approveGroupName, ResourcePermissionTypeEnum.CREATE_APPROVAL_GROUP);
        }
        approveGroupService.createApproveGroup(approveGroupName, description);
        return approveGroupService.listApproveGroup();
    }

    /**
     * modify approve group
     * @param approveGroupId approve group's id
     * @param approveGroupName name of approve group(could be new, should be unique)
     * @param description description
     * @return collection of approve group
     */
    @BctMethodInfo
    public Collection<ApproveGroupDTO> wkApproveGroupModify(
            @BctMethodArg String approveGroupId,
            @BctMethodArg String approveGroupName,
            @BctMethodArg String description){
        if (StringUtils.isBlank(approveGroupName)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND, approveGroupName);
        }
        ApproveGroupDTO approveGroup = approveGroupService.getApproveGroup(approveGroupId);

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(approveGroup.getApproveGroupName())
                        , APPROVAL_GROUP_INFO
                        , UPDATE_APPROVAL_GROUP).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.APPROVAL_GROUP_INFO, approveGroupName, ResourcePermissionTypeEnum.UPDATE_APPROVAL_GROUP);
        }

        approveGroupService.modifyApproveGroup(approveGroupId, approveGroupName, description);

        return approveGroupService.listApproveGroup();
    }

    /**
     * modify user list of approve group
     * @param approveGroupId approve group's id
     * @param approveGroupName name of approve group
     * @param userList user list of approve group
     * @return collection of approve group
     */
    @BctMethodInfo
    public Collection<ApproveGroupDTO> wkApproveGroupUserListModify(
            @BctMethodArg String approveGroupId,
            @BctMethodArg String approveGroupName,
            @BctMethodArg Collection<String> userList){
        if (StringUtils.isBlank(approveGroupId)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND, approveGroupId);
        }

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(approveGroupName)
                        , APPROVAL_GROUP_INFO
                        , UPDATE_APPROVAL_GROUP_USER).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.APPROVAL_GROUP_INFO, approveGroupName, ResourcePermissionTypeEnum.UPDATE_APPROVAL_GROUP_USER);
        }

        approveGroupService.modifyApproveGroupUserList(approveGroupId, userList);
        return approveGroupService.listApproveGroup();
    }

    /**
     * deletion of one approve group
     * @param approveGroupId approve group's id
     * @return success or not
     */
    @BctMethodInfo
    public String wkApproveGroupDelete(
            @BctMethodArg String approveGroupId){
        if (StringUtils.isBlank(approveGroupId)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND, approveGroupId);
        }

        ApproveGroupDTO approveGroup = approveGroupService.getApproveGroup(approveGroupId);

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(approveGroup.getApproveGroupName())
                        , APPROVAL_GROUP_INFO
                        , DELETE_APPROVAL_GROUP).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.APPROVAL_GROUP_INFO, approveGroup.getApproveGroupName()
                    , ResourcePermissionTypeEnum.DELETE_APPROVAL_GROUP);
        }

        approveGroupService.deleteApproveGroup(approveGroupId);

        return "success";
    }

    /**
     * list approve group
     * @return collection of approve group
     */
    @BctMethodInfo
    public Collection<ApproveGroupDTO> wkApproveGroupList(){
        return approveGroupService.listApproveGroup();
    }

    @BctMethodInfo
    public ProcessDTO wkTaskApproveGroupBind(
            @BctMethodArg String processName,
            @BctMethodArg List<Map<String, Object>> taskList){
        if (StringUtils.isBlank(processName) || Objects.isNull(taskList)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }

        if(!resourcePermissionAuthAction
                .hasResourcePermissionForCurrentUser(
                        Lists.newArrayList(APPROVAL_GROUP.getAlias())
                        , APPROVAL_GROUP
                        , UPDATE_TASK_NODE).get(0)){
            throw new AuthorizationException(ResourceTypeEnum.APPROVAL_GROUP, APPROVAL_GROUP.getAlias(), ResourcePermissionTypeEnum.UPDATE_TASK_NODE);
        }
        taskNodeService.bindTaskAndApproveGroup(processName, taskList);

        return ProcessDTO.of(processService.getProcessByProcessName(processName));
    }

}

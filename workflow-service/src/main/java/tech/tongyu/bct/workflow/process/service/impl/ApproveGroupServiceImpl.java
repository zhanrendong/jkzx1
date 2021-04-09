package tech.tongyu.bct.workflow.process.service.impl;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.manager.ResourceManager;
import tech.tongyu.bct.auth.manager.ResourcePermissionManager;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ApproveGroupDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.manager.ApproveGroupManager;
import tech.tongyu.bct.workflow.process.manager.TaskNodeManager;
import tech.tongyu.bct.workflow.process.service.ApproveGroupService;

import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum.Arrays.WHEN_CREATE_APPROVAL_GROUP;
import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.APPROVAL_GROUP;

/**
 * @author yongbin
 */
@Service
public class ApproveGroupServiceImpl implements ApproveGroupService {

    private ApproveGroupManager approveGroupManager;
    private TaskNodeManager taskNodeManager;
    private ResourceAuthAction resourceAuthAction;
    private ResourceManager resourceManager;
    private ResourcePermissionManager resourcePermissionManager;
    private UserManager userManager;

    @Autowired
    public ApproveGroupServiceImpl(
            ApproveGroupManager approveGroupManager
            , TaskNodeManager taskNodeManager
            , ResourceAuthAction resourceAuthAction
            , ResourceManager resourceManager
            , UserManager userManager
            , ResourcePermissionManager resourcePermissionManager) {
        this.approveGroupManager = approveGroupManager;
        this.taskNodeManager = taskNodeManager;
        this.resourceAuthAction = resourceAuthAction;
        this.resourceManager = resourceManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.userManager = userManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApproveGroupDTO createApproveGroup(String approveGroupName, String description) {
        Resource approveGroupResource = getApprovalGroupResource();
        ResourceDTO resourceDTO = resourceManager.createResource(
                approveGroupName
                , ResourceTypeEnum.APPROVAL_GROUP_INFO
                , approveGroupResource.getId(), null, 0);

        UserDTO userDTO = userManager.getCurrentUser();
        resourcePermissionManager.createResourcePermissions(
                userDTO.getId()
                , resourceDTO.getId()
                , WHEN_CREATE_APPROVAL_GROUP
        );

        return approveGroupManager.createApproveGroup(approveGroupName, description);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyApproveGroupUserList(String approveGroupId, Collection<String> username) {
        if (taskNodeManager.hasActiveCounterSignNodeByApproveGroupId(Lists.newArrayList(approveGroupId))) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.COUNTER_SIGN_GROUP_UPDATE_INFO);
        }
        if(CollectionUtils.isEmpty(username)){
            approveGroupManager.deleteUserApproveGroup(approveGroupId);
        } else {
            Collection<String> usernameSet = username.stream()
                    .map(name -> {
                        UserDTO userDTO = userManager.getUserOrNullByUserName(name);
                        if(Objects.isNull(userDTO)) {
                            return null;
                        }
                        return userDTO.getUsername();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            approveGroupManager.modifyUserApproveGroup(approveGroupId, usernameSet);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyApproveGroup(String approveGroupId, String approveGroupName, String description) {
        ApproveGroupDTO group = approveGroupManager.getApproveGroupByApproveGroupId(approveGroupId);
        //审批组名称更新则权限树更新
        if (!Objects.equals(group.getApproveGroupName(), approveGroupName)){
            Resource resource = getApprovalGroupResource();
            Resource modifyResource = getApprovalGroupByResourceName(resource, group.getApproveGroupName());
            resourceAuthAction.modifyResource(modifyResource.getId(), approveGroupName);
        }

        approveGroupManager.modifyApproveGroup(approveGroupId, approveGroupName, description);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApproveGroup(String approveGroupId) {
        if (!taskNodeManager.canDeleteGroup(approveGroupId)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.DELETE_GROUP_ERROR);
        }
        ApproveGroupDTO approveGroupDTO = approveGroupManager.getApproveGroupByApproveGroupId(approveGroupId);
        Resource resource = getApprovalGroupResource();
        Resource deleteResource = getApprovalGroupByResourceName(resource, approveGroupDTO.getApproveGroupName());
        resourceManager.deleteResourceByResourceId(deleteResource.getId());
        taskNodeManager.deleteTaskApproveGroupByApproveGroupId(approveGroupId);
        approveGroupManager.deleteUserApproveGroup(approveGroupId);
        approveGroupManager.deleteApproveGroup(approveGroupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<ApproveGroupDTO> listApproveGroup() {
       return approveGroupManager.listApproveGroup();
    }

    @Override
    public ApproveGroupDTO getApproveGroup(String approveGroupId) {
        return approveGroupManager.getApproveGroupByApproveGroupId(approveGroupId);
    }

    public Resource getApprovalGroupResource() {
        List<Resource> resources = resourceAuthAction.getResource().getChildren()
                .stream()
                .filter(children -> Objects.equals(APPROVAL_GROUP, children.getResourceType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resources)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.AUTH_APPROVAL_GROUP);
        }
        return resources.get(0);
    }

    public Resource getApprovalGroupByResourceName(Resource resource, String resourceName) {
        List<Resource> resources = resource.getChildren()
                .stream()
                .filter(children -> Objects.equals(resourceName, children.getResourceName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resources)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.AUTH_APPROVAL_GROUP_INFO, resourceName);
        }
        return resources.get(0);
    }
}

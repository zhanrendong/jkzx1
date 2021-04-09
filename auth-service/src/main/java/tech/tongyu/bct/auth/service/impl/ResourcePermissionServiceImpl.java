package tech.tongyu.bct.auth.service.impl;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.authaction.intel.RoleResourcePermissionAuthAction;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.RolePermissionDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.AuthBlankParamException;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.service.ResourcePermissionService;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ResourcePermissionServiceImpl implements ResourcePermissionService {

    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private ResourceAuthAction resourceAuthAction;
    private RoleResourcePermissionAuthAction roleResourcePermissionAuthAction;

    @Autowired
    public ResourcePermissionServiceImpl(
            ResourceAuthAction resourceAuthAction
            , ResourcePermissionAuthAction resourcePermissionAuthAction
            , RoleResourcePermissionAuthAction roleResourcePermissionAuthAction){
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
        this.resourceAuthAction = resourceAuthAction;
        this.roleResourcePermissionAuthAction = roleResourcePermissionAuthAction;
    }

    @Override
    @BctMethodInfo(
            description = "对用户资源增加权限，需要授权权限(GRANT_ACTION)",
            retName = "resource",
            retDescription = "资源信息",
            service = "auth-service"
    )
    @Transactional
    public Resource authPermissionsAdd(
            @BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_ID, description = "资源ID") String resourceId,
            @BctMethodArg(name = ApiParamConstants.PERMISSIONS, description = "权限列表", argClass = ResourcePermissionTypeEnum.class) List<String> permissions) {

        CommonUtils.checkBlankParam(new HashMap<String,String>() {{
            put(ApiParamConstants.USER_ID, userId);
            put(ApiParamConstants.RESOURCE_ID, resourceId);
        }});

        if(CollectionUtils.isEmpty(permissions))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE_PERMISSIONS);

        resourcePermissionAuthAction.createResourcePermissions(userId, resourceId, ResourcePermissionTypeEnum.ofList(permissions));

        return resourceAuthAction.getUserResource(userId);
    }

    @Override
    @BctMethodInfo(
            description = "更新用户资源权限，需要授权权限(GRANT_ACTION)",
            retName = "resource",
            retDescription = "资源信息",
            service = "auth-service"
    )
    @SuppressWarnings("unchecked")
    @Transactional
    public Resource authPermissionsModify(
            @BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId,
            @BctMethodArg(name = ApiParamConstants.PERMISSIONS, description = "权限树", argClass = RolePermissionDTO.class) List<Map<String, Object>> permissions){

        CommonUtils.checkBlankParam(new HashMap<String,String>() {{
            put(ApiParamConstants.USER_ID, userId);
        }});

        if(CollectionUtils.isEmpty(permissions))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE_PERMISSIONS);

        permissions.forEach(map -> {
            String resourceId = (String) map.get(ApiParamConstants.RESOURCE_ID);
            List<ResourcePermissionTypeEnum> resourcePermissionTypes = ResourcePermissionTypeEnum.ofList((List<String>) map.get(ApiParamConstants.RESOURCE_PERMISSION));
            resourcePermissionAuthAction.modifyResourcePermissions(userId, resourceId, resourcePermissionTypes);
        });

        return resourceAuthAction.getUserResource(userId);
    }

    @Override
    @BctMethodInfo(
            description = "批量判断是否拥有该权限",
            retName = "List of Boolean",
            retDescription = "true|false的列表",
            service = "auth-service"
    )
    @Transactional
    public List<Boolean> authCan(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "资源类型", argClass = ResourceTypeEnum.class) String resourceType,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_NAME, description = "资源名称") List<String> resourceName,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_PERMISSION, description = "资源权限类型") String resourcePermissionType
    ){

        CommonUtils.checkBlankParam(new HashMap<String,String>() {{
            put(ApiParamConstants.RESOURCE_TYPE, resourceType);
            put(ApiParamConstants.RESOURCE_PERMISSION_TYPE, resourcePermissionType);
        }});

        if(CollectionUtils.isEmpty(resourceName))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_PARAM_RESOURCE_NAME);

        return resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceName, ResourceTypeEnum.of(resourceType), ResourcePermissionTypeEnum.of(resourcePermissionType));
    }

    @Override
    @BctMethodInfo(
            description = "更新用户角色，需要更新用户权限(UPDATE_USER)",
            retName = "user",
            retDescription = "用户信息",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserDTO authUserRoleModify(
        @BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId,
        @BctMethodArg(name = ApiParamConstants.ROLE_IDS, description = "角色ID列表") List<String> roleIds
    ) {

        CommonUtils.checkBlankParam(new HashMap<String,String>() {{
            put(ApiParamConstants.USER_ID, userId);
        }});

        if(Objects.isNull(roleIds)){
            roleIds = Lists.newArrayList();
        }

        return resourcePermissionAuthAction.modifyUserRole(userId, roleIds);
    }

}

package tech.tongyu.bct.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.RolePermissionDTO;
import tech.tongyu.bct.auth.service.RoleResourcePermissionService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;

import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.authaction.intel.RoleResourcePermissionAuthAction;

import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

import java.util.List;
import java.util.Map;

@Service
public class RoleResourcePermissionServiceImpl implements RoleResourcePermissionService {

    private ResourceAuthAction resourceAuthAction;
    private RoleResourcePermissionAuthAction roleResourcePermissionAuthAction;

    @Autowired
    public RoleResourcePermissionServiceImpl(
            ResourceAuthAction resourceAuthAction
            , RoleResourcePermissionAuthAction roleResourcePermissionAuthAction){
        this.resourceAuthAction = resourceAuthAction;
        this.roleResourcePermissionAuthAction = roleResourcePermissionAuthAction;
    }

    @BctMethodInfo(
            description = "更新角色权限信息",
            retName = "resource tree of this role",
            retDescription = "角色的权限树",
            returnClass = Resource.class,
            service = "auth-service"
    )
    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public Resource authRolePermissionsModify(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID") String roleId,
            @BctMethodArg(name = ApiParamConstants.PERMISSIONS, description = "权限树", argClass = RolePermissionDTO.class) List<Map<String, Object>> permissions){
        if(CollectionUtils.isEmpty(permissions)) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE_PERMISSIONS);
        }
        permissions.forEach(map -> {
            String resourceId = (String) map.get(ApiParamConstants.RESOURCE_ID);
            List<ResourcePermissionTypeEnum> resourcePermissionTypes =
                    ResourcePermissionTypeEnum.ofList((List<String>) map.get(ApiParamConstants.RESOURCE_PERMISSION));
            roleResourcePermissionAuthAction.modifyRoleResourcePermissions(roleId, resourceId, resourcePermissionTypes);
        });

        return resourceAuthAction.getResourceByRoleId(roleId);
    }

}

package tech.tongyu.bct.auth.authaction.intel;

import tech.tongyu.bct.auth.dto.RoleResourcePermissionDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

import java.util.Collection;

public interface RoleResourcePermissionAuthAction {
    Collection<RoleResourcePermissionDTO> modifyRoleResourcePermissions(String roleId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypes);
}

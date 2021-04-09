package tech.tongyu.bct.auth.authaction.intel;

import tech.tongyu.bct.auth.dto.ResourcePermissionDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

import java.util.Collection;
import java.util.List;

public interface ResourcePermissionAuthAction {

    Boolean hasDepartmentResourcePermissionForCurrentUser(ResourcePermissionTypeEnum resourcePermissionType);

    Boolean hasDepartmentResourcePermissionForCurrentUser(String departmentId, ResourcePermissionTypeEnum resourcePermissionType);

    Boolean hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum resourcePermissionType);

    Collection<ResourcePermissionDTO> createResourcePermissions(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypeSet);

    Boolean hasResourcePermissionForCurrentUser(String resourceId, ResourcePermissionTypeEnum resourcePermissionType);

    List<Boolean> hasResourcePermissionForCurrentUser(String resourceName, ResourceTypeEnum resourceType, List<ResourcePermissionTypeEnum> resourcePermissionTypeList);

    List<Boolean> hasResourcePermissionForCurrentUser(List<String> resourceName, ResourceTypeEnum resourceTypeEnum, ResourcePermissionTypeEnum resourcePermissionType);

    Collection<ResourcePermissionDTO> modifyResourcePermissions(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypes);

    UserDTO modifyUserRole(String userId, List<String> roleIds);

    boolean hasPermission(String userId, String resourceId, ResourcePermissionTypeEnum resourcePermissionType);

    List<Boolean> hasPermission(String userId, String resourceId, List<ResourcePermissionTypeEnum> resourcePermissionTypeList);

    List<Boolean> hasPermission(String userId, List<String> resourceId, ResourcePermissionTypeEnum resourcePermissionType);
}

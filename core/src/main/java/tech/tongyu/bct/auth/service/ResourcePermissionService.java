package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface ResourcePermissionService {

    Resource authPermissionsAdd(String userId, String resourceId, List<String> permissions);

    Resource authPermissionsModify(String userId, List<Map<String, Object>> permissions);

    List<Boolean> authCan(String resourceType, List<String> resourceName, String resourcePermissionType);

    UserDTO authUserRoleModify(String userId, List<String> roleIds);
}

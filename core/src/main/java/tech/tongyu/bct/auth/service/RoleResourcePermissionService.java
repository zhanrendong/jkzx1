package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.Resource;

import java.util.List;
import java.util.Map;

public interface RoleResourcePermissionService {

    Resource authRolePermissionsModify(String roleId, List<Map<String, Object>> permissions);
}

package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.RoleDTO;
import tech.tongyu.bct.auth.dto.UserDTO;

import java.util.Collection;

public interface RoleService {

    RoleDTO authRoleCreate(String roleName, String alias, String remark);

    Boolean authRoleRevoke(String roleId);

    RoleDTO authRoleUpdate(String roleId, String roleName, String alias, String remark);

    Collection<RoleDTO> authRoleList();

    RoleDTO authRoleGet(String roleId);

    RoleDTO authRoleGetByRoleName(String roleName);

    Collection<UserDTO> authUserListByRoleId(String roleId);

    Collection<RoleDTO> authRoleListByCurrentUser();
}

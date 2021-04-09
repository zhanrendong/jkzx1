package tech.tongyu.bct.auth.authaction.intel;

import tech.tongyu.bct.auth.dto.RoleDTO;
import tech.tongyu.bct.auth.dto.UserDTO;

import java.util.Collection;

public interface RoleAuthAction {

    RoleDTO createRole(String roleName, String alias, String remark);

    void deleteRole(String roleId);

    RoleDTO updateRole(String roleId, String roleName, String alias, String remark);

    Collection<RoleDTO> listRoles();

    RoleDTO getRoleByRoleId(String roleId);

    RoleDTO getRoleByRoleName(String roleName);

    Collection<UserDTO> getUserListByRoleId(String roleId);

    Collection<RoleDTO> getRoleListByCurrentUser();
}

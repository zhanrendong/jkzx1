package tech.tongyu.bct.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.RoleAuthAction;
import tech.tongyu.bct.auth.dto.RoleDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.service.RoleService;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;

import java.util.Collection;
import java.util.HashMap;

@Service
public class RoleServiceImpl implements RoleService {

    private RoleAuthAction roleAuthAction;

    @Autowired
    public RoleServiceImpl(RoleAuthAction roleAuthAction){
        this.roleAuthAction = roleAuthAction;
    }

    @Override
    @BctMethodInfo(
            description = "创建角色，需要创建角色权限(CREATE_ROLE)",
            retName = "role",
            retDescription = "角色信息",
            returnClass = RoleDTO.class,
            service = "auth-service"
    )
    @Transactional
    public RoleDTO authRoleCreate(
            @BctMethodArg(name = ApiParamConstants.ROLE_NAME, description = "角色名") String roleName,
            @BctMethodArg(name = ApiParamConstants.ALIAS, description = "角色别名", required = false) String alias,
            @BctMethodArg(name = ApiParamConstants.REMARK, description = "备注", required = false) String remark) {

         CommonUtils.checkBlankParam(new HashMap<String, String>() {{
             put(ApiParamConstants.ROLE_NAME, roleName);
         }});

        return roleAuthAction.createRole(roleName, alias, remark);
    }

    @Override
    @BctMethodInfo(
            description = "删除角色，需要删除角色权限(DELETE_ROLE)",
            retName = "success or failure",
            retDescription = "删除结果",
            service = "auth-service"
    )
    @Transactional
    public Boolean authRoleRevoke(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID") String roleId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.ROLE_ID, roleId);
        }});

        roleAuthAction.deleteRole(roleId);
        return true;
    }

    @Override
    @BctMethodInfo(
            description = "更新角色，需要更新角色权限(UPDATE_ROLE)",
            retName = "role",
            retDescription = "角色信息",
            returnClass = RoleDTO.class,
            service = "auth-service"
    )
    @Transactional
    public RoleDTO authRoleUpdate(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID") String roleId,
            @BctMethodArg(name = ApiParamConstants.ROLE_NAME, description = "角色名") String roleName,
            @BctMethodArg(name = ApiParamConstants.ALIAS, description = "别名") String alias,
            @BctMethodArg(name = ApiParamConstants.REMARK, description = "备注") String remark) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.ROLE_ID, roleId);
            put(ApiParamConstants.ROLE_NAME, roleName);
        }});

        return roleAuthAction.updateRole(roleId, roleName, alias, remark);
    }

    @Override
    @BctMethodInfo(
            description = "获取全部角色",
            retName = "roles",
            retDescription = "角色信息列表",
            returnClass = RoleDTO.class,
            service = "auth-service"
    )
    public Collection<RoleDTO> authRoleList() {
        return roleAuthAction.listRoles();
    }

    @Override
    @BctMethodInfo(
            description = "根据角色ID获取角色信息",
            retName = "role",
            retDescription = "角色信息",
            returnClass = RoleDTO.class,
            service = "auth-service"
    )
    public RoleDTO authRoleGet(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID") String roleId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.ROLE_ID, roleId);
        }});

        return roleAuthAction.getRoleByRoleId(roleId);
    }

    @Override
    @BctMethodInfo(
            description = "根据角色名获取角色信息",
            retName = "role",
            retDescription = "角色信息",
            returnClass = RoleDTO.class,
            service = "auth-service"
    )
    public RoleDTO authRoleGetByRoleName(
            @BctMethodArg(name = ApiParamConstants.ROLE_NAME, description = "角色名") String roleName) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.ROLE_NAME, roleName);
        }});

        return roleAuthAction.getRoleByRoleName(roleName);
    }

    @Override
    @BctMethodInfo(
            description = "根据角色ID获取用户信息列表，需要查询用户权限(READ_USER)",
            retName = "users",
            retDescription = "用户信息列表",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    public Collection<UserDTO> authUserListByRoleId(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID") String roleId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.ROLE_ID, roleId);
        }});

        return roleAuthAction.getUserListByRoleId(roleId);
    }

    @Override
    @BctMethodInfo(
            description = "获取当前用户的角色列表",
            retName = "roles",
            retDescription = "角色信息列表",
            returnClass = RoleDTO.class,
            service = "auth-service"
    )
    public Collection<RoleDTO> authRoleListByCurrentUser() {
        return roleAuthAction.getRoleListByCurrentUser();
    }
}

package tech.tongyu.bct.auth.authaction.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.authaction.intel.RoleAuthAction;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.ResourcePermissionDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.*;
import tech.tongyu.bct.auth.dto.RoleDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class RoleAuthActionImpl implements RoleAuthAction {

    private RoleManager roleManager;
    private UserManager userManager;
    private ResourceManager resourceManager;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private DepartmentManager departmentManager;
    private ResourcePermissionManager resourcePermissionManager;

    @Autowired
    public RoleAuthActionImpl(
            RoleManager roleManager
            , UserManager userManager
            , ResourceManager resourceManager
            , DepartmentManager departmentManager
            , ResourcePermissionManager resourcePermissionManager
            , ResourcePermissionAuthAction resourcePermissionAuthAction){
        this.userManager = userManager;
        this.departmentManager = departmentManager;
        this.resourceManager = resourceManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.roleManager = roleManager;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
    }

    @Override
    public RoleDTO createRole(String roleName, String alias, String remark) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.CREATE_ROLE)) {
            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.CREATE_ROLE);
        }

        if (roleManager.isRoleExist(roleName)) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.DUPLICATE_ROLE_NAME, roleName);
        }
        return roleManager.createRole(roleName, alias, remark);
    }

    @Override
    public void deleteRole(String roleId) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.DELETE_ROLE)) {
            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.DELETE_ROLE);
        }

        if (roleManager.getValidRoleWithRoleId(roleId).getRoleName().equals(AuthConstants.ADMIN))
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.DELETE_ADMIN_ROLE);

        roleManager.removeRole(roleId);
    }

    @Override
    public RoleDTO updateRole(String roleId, String roleName, String alias, String remark) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.UPDATE_ROLE)) {
            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.UPDATE_ROLE);
        }

        if (roleManager.getValidRoleWithRoleId(roleId).getRoleName().equals(AuthConstants.ADMIN)
                && !AuthConstants.ADMIN.equals(roleName)) {

            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.UPDATE_ADMIN_ROLE_NAME);
        }

        return roleManager.updateRole(roleId, roleName, alias, remark);
    }

    @Override
    public Collection<RoleDTO> listRoles() {
        return roleManager.listAllValidRoles();
    }

    @Override
    public RoleDTO getRoleByRoleId(String roleId) {
        return roleManager.getValidRoleWithRoleId(roleId);
    }

    @Override
    public RoleDTO getRoleByRoleName(String roleName) {
        return roleManager.getValidRoleWithRoleName(roleName);
    }

    @Override
    public Collection<UserDTO> getUserListByRoleId(String roleId) {

        UserDTO userDto = userManager.getCurrentUser();

        Collection<String> resourceIds = resourcePermissionManager
                .listResourcePermissionByUserIdsAndResourcePermissionType(userDto.getId(), ResourcePermissionTypeEnum.READ_USER)
                .stream().map(ResourcePermissionDTO::getResourceId).collect(Collectors.toSet());

        Collection<String> departmentIds = resourceManager.listResourceByResourceId(resourceIds)
                .stream().map(ResourceDTO::getDepartmentId).collect(Collectors.toSet());

        return roleManager.getUserListByRoleId(roleId, departmentIds);
    }

    @Override
    public Collection<RoleDTO> getRoleListByCurrentUser(){
        UserDTO userDto = userManager.getCurrentUser();
        return roleManager.listValidRolesByRoleNameList(userDto.getRoleName());
    }
}

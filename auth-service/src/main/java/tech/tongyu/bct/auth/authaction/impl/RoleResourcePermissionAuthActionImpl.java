package tech.tongyu.bct.auth.authaction.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.authaction.intel.RoleResourcePermissionAuthAction;
import tech.tongyu.bct.auth.config.Constants;
import tech.tongyu.bct.auth.dto.*;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.*;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RoleResourcePermissionAuthActionImpl implements RoleResourcePermissionAuthAction {

    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private ResourcePermissionManager resourcePermissionManager;
    private RoleResourcePermissionManager roleResourcePermissionManager;
    private ResourceManager resourceManager;
    private RoleManager roleManager;
    private UserManager userManager;
    private DepartmentManager departmentManager;

    public RoleResourcePermissionAuthActionImpl(
            ResourcePermissionAuthAction resourcePermissionAuthAction
            , ResourcePermissionManager resourcePermissionManager
            , RoleResourcePermissionManager roleResourcePermissionManager
            , ResourceManager resourceManager
            , RoleManager roleManager
            , UserManager userManager
            , DepartmentManager departmentManager){
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
        this.resourcePermissionManager = resourcePermissionManager;
        this.roleResourcePermissionManager = roleResourcePermissionManager;
        this.resourceManager = resourceManager;
        this.roleManager = roleManager;
        this.userManager = userManager;
        this.departmentManager = departmentManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Collection<RoleResourcePermissionDTO> modifyRoleResourcePermissions(String roleId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypes) {
        if(!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceId, ResourcePermissionTypeEnum.GRANT_ACTION)
                && !resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.GRANT_ACTION)) {

            ResourceDTO resource = resourceManager.getResource(resourceId);
            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), ResourcePermissionTypeEnum.GRANT_ACTION);
        }

        Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnums = roleResourcePermissionManager.listResourcePermissionTypeByRoleIdAndResourceId(roleId, resourceId);
        Collection<RoleResourcePermissionDTO> roleResourcePermissionDTOs = roleResourcePermissionManager.modifyRoleResourcePermissions(roleId, resourceId, resourcePermissionTypes);

        roleManager.getUserListByRoleId(roleId).stream().forEach(user -> {
            resourcePermissionManager.deleteResourcePermissions(user.getId(), resourceId, resourcePermissionTypeEnums);

            List<String> roleIds = roleManager.listValidRolesByRoleNameList(user.getRoleName()).stream().map(RoleDTO::getId).collect(Collectors.toList());
            Collection<ResourcePermissionDTO> permissions = resourcePermissionManager.listResourcePermissionByUserId(user.getId());
            Collection<RoleResourcePermissionDTO> roleResourcePermissions = roleResourcePermissionManager.getRoleResourcePermissions(roleIds);

            if (Objects.equals(user.getUsername(), Constants.ADMIN)) {
                RoleDTO adminRole = roleManager.getValidRoleWithRoleName(Constants.ADMIN);
                if (roleResourcePermissions.stream().noneMatch(v -> Objects.equals(adminRole.getId(), v.getRoleId()))) {
                    throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.FORBIDDEN_DELETE_ADMIN_ROLE_FOR_ADMIN);
                }
            }

            Set<ResourcePermissionDTO> toAddPermissions = roleResourcePermissions.stream()
                    .map(v -> ConverterUtils.getResourcePermissionDto(user.getId(), v)).collect(Collectors.toSet());

            List<String> oldRoleIds = userManager.findUserByUserId(user.getId()).getRoleName().stream()
                    .map(v -> Optional.ofNullable(roleManager.getValidRoleWithRoleName(v)).map(RoleDTO::getId)
                            .orElseThrow(() -> new AuthServiceException(ReturnMessageAndTemplateDef.Errors.MISSING_ROLE, v)))
                    .filter(v -> !roleIds.contains(v))
                    .collect(Collectors.toList());

            Set<ResourcePermissionDTO> toRemovePermissions = roleResourcePermissionManager.getRoleResourcePermissions(oldRoleIds).stream()
                    .map(v -> ConverterUtils.getResourcePermissionDto(user.getId(), v))
                    .filter(v -> !toAddPermissions.contains(v))
                    .collect(Collectors.toSet());

            permissions.removeAll(toRemovePermissions);
            permissions.addAll(toAddPermissions);

            String companyResourceId = departmentManager.getDepartmentWithResource(departmentManager.getCompanyDepartmentId().orElseThrow(
                    () -> new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_COMPANY_INFO)
            )).getResourceId();
            boolean userIsAdmin = Objects.equals(user.getUsername(), Constants.ADMIN);

            toRemovePermissions.removeAll(permissions);
            toRemovePermissions.stream().map(ResourcePermissionDTO::getResourceId)
                    .forEach(v -> {
                        if (userIsAdmin && Objects.equals(v, companyResourceId)) {
                            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.WEAKEN_PERMISIONS_OF_ADMIN);
                        }
                        resourcePermissionManager.modifyResourcePermissions(user.getId(), v, Lists.newArrayList());
                    });

            permissions.stream().collect(Collectors.groupingBy(ResourcePermissionDTO::getResourceId))
                    .forEach((k, v) -> {
                        Set<ResourcePermissionTypeEnum> permissionTypeEnums = v.stream().map(ResourcePermissionDTO::getResourcePermission).collect(Collectors.toSet());
                        if (userIsAdmin && Objects.equals(k, companyResourceId)
                                && !permissionTypeEnums.containsAll(ResourcePermissionTypeEnum.Arrays.ADMIN_ON_COMPANY)) {

                            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.WEAKEN_PERMISIONS_OF_ADMIN);
                        }

                        resourcePermissionManager.modifyResourcePermissions(user.getId(), k, permissionTypeEnums);
                    });

            userManager.updateUserRoles(user.getId(), roleIds);
        });

        return roleResourcePermissionDTOs;
    }
}

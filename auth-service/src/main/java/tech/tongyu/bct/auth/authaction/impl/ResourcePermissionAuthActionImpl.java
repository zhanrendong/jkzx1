package tech.tongyu.bct.auth.authaction.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.cache.ResourcePermissionCacheManager;
import tech.tongyu.bct.auth.config.Constants;
import tech.tongyu.bct.auth.dto.*;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.enums.UserTypeEnum;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.*;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ResourcePermissionAuthActionImpl implements ResourcePermissionAuthAction {
    private ResourcePermissionManager resourcePermissionManager;
    private UserManager userManager;
    private DepartmentManager departmentManager;
    private RoleResourcePermissionManager roleResourcePermissionManager;
    private ResourceManager resourceManager;
    private RoleManager roleManager;
    private ResourcePermissionCacheManager resourcePermissionCacheManager;

    @Autowired
    public ResourcePermissionAuthActionImpl(
            ResourcePermissionManager resourcePermissionManager
            , RoleResourcePermissionManager roleResourcePermissionManager
            , UserManager userManager
            , DepartmentManager departmentManager
            , ResourceManager resourceManager
            , ResourcePermissionCacheManager resourcePermissionCacheManager
            , RoleManager roleManager){
        this.roleResourcePermissionManager = roleResourcePermissionManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.resourceManager = resourceManager;
        this.departmentManager = departmentManager;
        this.userManager = userManager;
        this.resourcePermissionCacheManager = resourcePermissionCacheManager;
        this.roleManager = roleManager;
    }

    @Transactional
    public Boolean hasDepartmentResourcePermissionForCurrentUser(ResourcePermissionTypeEnum resourcePermissionType){
        UserDTO currentUserDto = userManager.getCurrentUser();
        DepartmentWithResourceDTO departmentWithResourceDto = departmentManager.getDepartmentWithResource(currentUserDto.getDepartmentId());
        return hasPermission(currentUserDto.getId(), departmentWithResourceDto.getResourceId(), resourcePermissionType);
    }

    @Transactional
    public Boolean hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum resourcePermissionType){
        UserDTO currentUserDto = userManager.getCurrentUser();
        DepartmentWithResourceDTO departmentWithResourceDto = departmentManager.getDepartmentWithResource(
                departmentManager.getDepartmentByDepartmentNameAndParentId(departmentManager.getCompanyInfo().getCompanyName(), null).getId());
        return hasPermission(currentUserDto.getId(), departmentWithResourceDto.getResourceId(), resourcePermissionType);
    }

    @Override
    @Transactional
    public Boolean hasDepartmentResourcePermissionForCurrentUser(String departmentId, ResourcePermissionTypeEnum resourcePermissionType) {
        UserDTO currentUserDto = userManager.getCurrentUser();
        DepartmentWithResourceDTO departmentWithResourceDto = departmentManager.getDepartmentWithResource(departmentId);
        return hasPermission(currentUserDto.getId(), departmentWithResourceDto.getResourceId(), resourcePermissionType);
    }

    @Transactional
    public Boolean hasResourcePermissionForCurrentUser(String resourceId, ResourcePermissionTypeEnum resourcePermissionType){
        UserDTO currentUserDto = userManager.getCurrentUser();
        return hasPermission(currentUserDto.getId(), resourceId, resourcePermissionType);
    }

    @Override
    @Transactional
    public UserDTO modifyUserRole(String userId, List<String> roleIds) {
        Department companyDepartment = departmentManager.getCompanyDepartment();
        if (!hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.UPDATE_USER)) {
            String departmentName = companyDepartment.getDepartmentName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, departmentName, ResourcePermissionTypeEnum.UPDATE_USER);
        }

        Collection<ResourcePermissionDTO> permissions = resourcePermissionManager.listResourcePermissionByUserId(userId);

        Collection<RoleResourcePermissionDTO> roleResourcePermissions = roleResourcePermissionManager.getRoleResourcePermissions(roleIds);
        UserDTO user = userManager.getUserByUserId(userId);
        if (Objects.equals(user.getUsername(), Constants.ADMIN)) {
            RoleDTO adminRole = roleManager.getValidRoleWithRoleName(Constants.ADMIN);
            if (roleResourcePermissions.stream().noneMatch(v -> Objects.equals(adminRole.getId(), v.getRoleId()))) {
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.FORBIDDEN_DELETE_ADMIN_ROLE_FOR_ADMIN);
            }
        }

        Set<ResourcePermissionDTO> toAddPermissions = roleResourcePermissions.stream()
                .map(v -> ConverterUtils.getResourcePermissionDto(userId, v)).collect(Collectors.toSet());

        List<String> oldRoleIds = userManager.findUserByUserId(userId).getRoleName().stream()
                .map(v -> Optional.ofNullable(roleManager.getValidRoleWithRoleName(v)).map(RoleDTO::getId)
                        .orElseThrow(() -> new AuthServiceException(ReturnMessageAndTemplateDef.Errors.MISSING_ROLE, v)))
                .filter(v -> !roleIds.contains(v))
                .collect(Collectors.toList());

        Set<ResourcePermissionDTO> toRemovePermissions = roleResourcePermissionManager.getRoleResourcePermissions(oldRoleIds).stream()
                .map(v -> ConverterUtils.getResourcePermissionDto(userId, v))
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
                    resourcePermissionManager.modifyResourcePermissions(userId, v, Lists.newArrayList());
                });

        permissions.stream().collect(Collectors.groupingBy(ResourcePermissionDTO::getResourceId))
                .forEach((k, v) -> {
                    Set<ResourcePermissionTypeEnum> permissionTypeEnums = v.stream().map(ResourcePermissionDTO::getResourcePermission).collect(Collectors.toSet());
                    if (userIsAdmin && Objects.equals(k, companyResourceId)
                            && !permissionTypeEnums.containsAll(ResourcePermissionTypeEnum.Arrays.ADMIN_ON_COMPANY)) {

                        throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.WEAKEN_PERMISIONS_OF_ADMIN);
                    }

                    resourcePermissionManager.modifyResourcePermissions(userId, k, permissionTypeEnums);
                });

        return userManager.updateUserRoles(userId, roleIds);
    }

    @Override
    @Transactional
    public Collection<ResourcePermissionDTO> createResourcePermissions(String userId, String resourceId
            , Collection<ResourcePermissionTypeEnum> resourcePermissionTypeSet) {

        if(!hasResourcePermissionForCurrentUser(resourceId, ResourcePermissionTypeEnum.GRANT_ACTION)
                && !hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.GRANT_ACTION)) {

            ResourceDTO resource = resourceManager.getResource(resourceId);
            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), ResourcePermissionTypeEnum.GRANT_ACTION);
        }

        return resourcePermissionManager.createResourcePermissions(userId, resourceId, resourcePermissionTypeSet);
    }

    @Override
    @Transactional
    public Collection<ResourcePermissionDTO> modifyResourcePermissions(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypes) {

        ResourceDTO resource = resourceManager.getResource(resourceId);
        if(!hasResourcePermissionForCurrentUser(resourceId, ResourcePermissionTypeEnum.GRANT_ACTION)
                && !hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.GRANT_ACTION)) {

            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), ResourcePermissionTypeEnum.GRANT_ACTION);
        }

        String companyResourceId = departmentManager.getDepartmentWithResource(departmentManager.getCompanyDepartmentId().orElseThrow(
                () -> new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_COMPANY_INFO)
        )).getResourceId();
        if (Objects.equals(userManager.getUserByUserName(Constants.ADMIN).getId(), userId) && Objects.equals(resourceId, companyResourceId)
                && !resourcePermissionTypes.containsAll(ResourcePermissionTypeEnum.Arrays.ADMIN_ON_COMPANY)) {

            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.WEAKEN_PERMISIONS_OF_ADMIN);
        }

        return resourcePermissionManager.modifyResourcePermissions(userId, resourceId, resourcePermissionTypes);

    }

    @Override
    @Transactional
    public List<Boolean> hasResourcePermissionForCurrentUser(String resourceName, ResourceTypeEnum resourceType, List<ResourcePermissionTypeEnum> resourcePermissionTypeList) {
        UserDTO currentUserDto = userManager.getCurrentUser();
        ResourceDTO resourceDto = resourceManager.getResource(resourceName, resourceType);
        return hasPermission(currentUserDto.getId(), resourceDto.getId(), resourcePermissionTypeList);
    }

    @Override
    @Transactional
    public List<Boolean> hasResourcePermissionForCurrentUser(List<String> resourceName, ResourceTypeEnum resourceTypeEnum, ResourcePermissionTypeEnum resourcePermissionType) {
        UserDTO currentUserDto = userManager.getCurrentUser();

        List<String> resourceIdList = resourceManager.listResource(resourceName, resourceTypeEnum)
                .stream()
                .map(resourceDto -> (resourceDto == null) ? null : resourceDto.getId())
                .collect(Collectors.toList());
        return hasPermission(currentUserDto.getId(), resourceIdList, resourcePermissionType);
    }

    @Override
    @Transactional
    public boolean hasPermission(String userId, String resourceId, ResourcePermissionTypeEnum resourcePermissionType){
        if (scriptUserPermissionTestForBookAndTrade(userManager.getUserByUserId(userId), resourcePermissionType)) {
            return true;
        }
        return resourcePermissionCacheManager.hasPermission(userId, resourceId, resourcePermissionType);
    }

    @Override
    @Transactional
    public List<Boolean> hasPermission(String userId, String resourceId, List<ResourcePermissionTypeEnum> resourcePermissionTypeList) {
        UserDTO userDTO = userManager.getUserByUserId(userId);
        List<Boolean> scriptUserPermissionMask = resourcePermissionTypeList.stream()
                .map(v -> scriptUserPermissionTestForBookAndTrade(userDTO, v))
                .collect(Collectors.toList());
        List<Boolean> permissionList = resourcePermissionCacheManager.hasPermission(userId, resourceId, resourcePermissionTypeList);
        for (int i = 0; i < permissionList.size(); i++) {
            permissionList.set(i, permissionList.get(i) || scriptUserPermissionMask.get(i));
        }

        return permissionList;
    }

    @Override
    @Transactional
    public List<Boolean> hasPermission(String userId, List<String> resourceId, ResourcePermissionTypeEnum resourcePermissionType) {
        if (scriptUserPermissionTestForBookAndTrade(userManager.getUserByUserId(userId), resourcePermissionType)) {
            List<Boolean> result = Lists.newArrayList();
            for (int i = 0; i < resourceId.size(); i++) {
                result.add(true);
            }
            return result;
        }
        return resourcePermissionCacheManager.hasPermission(userId, resourceId, resourcePermissionType);
    }

    /** 只有当用户类型为[脚本用户]且权限类型为{读取交易簿，读取交易}之一时返回true */
    private Boolean scriptUserPermissionTestForBookAndTrade(UserDTO userDTO, ResourcePermissionTypeEnum resourcePermissionTypeEnum) {
        if (UserTypeEnum.SCRIPT.equals(userDTO.getUserType())) {
            return Sets.newHashSet(
                    ResourcePermissionTypeEnum.READ_BOOK,
                    ResourcePermissionTypeEnum.READ_TRADE
            ).contains(resourcePermissionTypeEnum);
        }
        return false;
    }

}

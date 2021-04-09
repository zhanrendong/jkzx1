package tech.tongyu.bct.auth.authaction.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.acl.common.UserInfo;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.cache.ResourcePermissionCacheManager;
import tech.tongyu.bct.auth.dto.DepartmentWithResourceDTO;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.DepartmentManager;
import tech.tongyu.bct.auth.manager.ResourceManager;
import tech.tongyu.bct.auth.manager.ResourcePermissionManager;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.common.util.BeanUtil;
import tech.tongyu.bct.common.util.tree.TreeEntity;

import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.auth.enums.ResourceTypeEnum.BOOK;

@Component
public class ResourceAuthActionImpl implements ResourceAuthAction {

    private ResourceManager resourceManager;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private ResourcePermissionManager resourcePermissionManager;
    private ResourcePermissionCacheManager resourcePermissionCacheManager;
    private UserManager userManager;
    private DepartmentManager departmentManager;

    @Autowired
    public ResourceAuthActionImpl(
            ResourceManager resourceManager
            , DepartmentManager departmentManager
            , UserManager userManager
            , ResourcePermissionManager resourcePermissionManager
            , ResourcePermissionCacheManager resourcePermissionCacheManager
            , ResourcePermissionAuthAction resourcePermissionAuthAction){
        this.departmentManager = departmentManager;
        this.resourceManager = resourceManager;
        this.userManager = userManager;
        this.resourcePermissionCacheManager = resourcePermissionCacheManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
    }

    @Override
    @Transactional
    public void revokeResource(String resourceName, ResourceTypeEnum resourceType){
        ResourcePermissionTypeEnum resourcePermissionType;
        switch (resourceType){
            case BOOK:
                resourcePermissionType = ResourcePermissionTypeEnum.DELETE_BOOK;
                break;
            case PORTFOLIO:
                resourcePermissionType = ResourcePermissionTypeEnum.DELETE_PORTFOLIO;
                break;
            default:
                resourcePermissionType = ResourcePermissionTypeEnum.DELETE_BOOK;
        }
        ResourceDTO resourceDto = resourceManager.getResource(resourceName, resourceType);

        if(!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceDto.getId(), resourcePermissionType)){
            throw new AuthorizationException(resourceDto.getResourceType(), resourceDto.getResourceName(), resourcePermissionType);
        }

        resourceManager.deleteResourceByResourceNameAndResourceType(resourceName, resourceType);
    }

    @Override
    @Transactional
    public void modifyNonGroupResourceName(String resourceName, ResourceTypeEnum resourceType, String newResourceName) {
        ResourcePermissionTypeEnum resourcePermissionType;
        switch (resourceType){
            case BOOK:
                resourcePermissionType = ResourcePermissionTypeEnum.UPDATE_BOOK;
                break;
            case PORTFOLIO:
                resourcePermissionType = ResourcePermissionTypeEnum.UPDATE_PORTFOLIO;
                break;
            default:
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.WRONG_RESOURCE_TYPE);
        }

        if (resourceManager.listAllResource().stream().anyMatch(v -> Objects.equals(v.getResourceName(), newResourceName)
                && Objects.equals(v.getResourceType(), resourceType))) {

            throw new AuthServiceException(

                    ReturnMessageAndTemplateDef.Errors.DUPLICATE_NON_GROUP_RESOURCE,
                    resourceName, resourceType.name()
            );
        }

        ResourceDTO resourceDto = resourceManager.getResource(resourceName, resourceType);
        if(!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceDto.getId(), resourcePermissionType)){
            throw new AuthorizationException(resourceDto.getResourceType(), resourceDto.getResourceName(), resourcePermissionType);
        }

        resourceManager.modifyResourceNameByResourceId(resourceDto.getId(), newResourceName);
    }

    @Override
    @Transactional
    public ResourceDTO createNonGroupResource(String resourceName, ResourceTypeEnum resourceType, String departmentId, Integer sort) {
        ResourcePermissionTypeEnum resourcePermissionType;
        List<ResourcePermissionTypeEnum> initialPermissionTypes;
        switch (resourceType){
            case BOOK:
                resourcePermissionType = ResourcePermissionTypeEnum.CREATE_BOOK;
                initialPermissionTypes = ResourcePermissionTypeEnum.Arrays.WHEN_CREATE_BOOK;
                break;
            case PORTFOLIO:
                resourcePermissionType = ResourcePermissionTypeEnum.CREATE_PORTFOLIO;
                initialPermissionTypes = ResourcePermissionTypeEnum.Arrays.WHEN_CREATE_PORTFOLIO;
                break;
            default:
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.WRONG_RESOURCE_TYPE);
        }

        String parentId = departmentManager.getDepartmentWithResource(departmentId).getResourceId();

        if(!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(parentId, resourcePermissionType)) {
            ResourceDTO resource = resourceManager.getResource(parentId);
            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), resourcePermissionType);
        }

        if (ResourceTypeEnum.BOOK.equals(resourceType) && parentId == null)
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.BOOK_IN_COMPANY_NOT_PERMITTED);

        if (resourceManager.listAllValidResource().stream().anyMatch(v ->
                Objects.equals(v.getResourceName(), resourceName)
                && Objects.equals(v.getResourceType(), resourceType)
                //&& Objects.equals(v.getParentId(), parentId)
        )) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.DUPLICATE_NON_GROUP_RESOURCE, resourceName, resourceType.name());
        }

        ResourceDTO resource = resourceManager.createResource(resourceName, resourceType, parentId, departmentId, sort);
        resourcePermissionManager.createResourcePermissions(userManager.getCurrentUser().getId(), resource.getId(), initialPermissionTypes);
        return resource;
    }

    @Override
    public Resource getRoleResource(String roleId) {
        return resourceManager.getResourceTreeByRoleId(roleId);
    }

    @Transactional
    public Resource getResource(){
        UserDTO userDto = userManager.getCurrentUser();
        return TreeEntity.fromRecords(
                resourceManager.listAllValidResource()
                , (resourceDto, parent) -> {
                    ResourceDTO dto = (ResourceDTO) resourceDto;
                    return new Resource(resourceDto.getId(), resourceDto.getSort(), parent
                            , dto.getResourceName(), dto.getResourceType(), dto.getDepartmentId()
                            , resourcePermissionManager.listResourcePermissionTypeByUserIdAndResourceId(userDto.getId(), dto.getId())
                    );
                }
        );
    }

    @Transactional
    public Resource getUserResource(String userId){
        return TreeEntity.fromRecords(
                resourceManager.listAllValidResource()
                , (resourceDto, parent) -> {
                    ResourceDTO dto = (ResourceDTO) resourceDto;
                    return new Resource(resourceDto.getId(), resourceDto.getSort(), parent
                            , dto.getResourceName(), dto.getResourceType(), dto.getDepartmentId()
                            , resourcePermissionManager.listResourcePermissionTypeByUserIdAndResourceId(userId, dto.getId())
                    );
                }
        );
    }

    @Override
    public Resource getResourceByRoleId(String roleId) {
        return resourceManager.getResourceTreeByRoleId(roleId);
    }

    @Override
    public Resource getResourceByUserId(String userId) {
        return resourceManager.getResourceTreeByUserId(userId);
    }

    @Override
    public Resource deleteResource(String resourceId) {
        ResourceDTO resource = resourceManager.getResource(resourceId);
        String parentId = resource.getParentId();

        // 该资源为root资源
        if (Objects.isNull(parentId)) {
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.DELETE_ROOT_RESOURCE_NOT_PERMITTED);
        }

        ResourceTypeEnum resourceType = resource.getResourceType();

        ResourcePermissionTypeEnum resourcePermissionType;
        switch (resourceType){
            case BOOK:
                resourcePermissionType = ResourcePermissionTypeEnum.DELETE_BOOK;
                break;
            case PORTFOLIO:
                resourcePermissionType = ResourcePermissionTypeEnum.DELETE_PORTFOLIO;
                break;
            default:
                resourcePermissionType = ResourcePermissionTypeEnum.DELETE_NAMESPACE;
        }

        if(!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceId, resourcePermissionType)) {
            throw new AuthorizationException(resourceType, resource.getResourceName(), resourcePermissionType);
        }

        resourceManager.deleteResourceByResourceId(resourceId);
        return resourceManager.getResourceTree();
    }

    @Override
    public ResourceDTO createResource(String resourceName, ResourceTypeEnum resourceType, String parentId, Integer sort) {
        ResourcePermissionTypeEnum resourcePermissionTypeEnum = ResourcePermissionTypeEnum.getCreateResourcePermission(resourceType);
        if(!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(parentId, resourcePermissionTypeEnum)) {
            ResourceDTO resource = resourceManager.getResource(parentId);
            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), resourcePermissionTypeEnum);
        }

        if (resourceManager.listAllValidResource().stream().anyMatch(v ->
                Objects.equals(v.getResourceName(), resourceName)
                && Objects.equals(v.getResourceType(), resourceType)
                && Objects.equals(v.getParentId(), parentId))) {

            throw new AuthServiceException(
                    ReturnMessageAndTemplateDef.Errors.DUPLICATE_RESOURCE,
                    resourceName, resourceType.name()
            );
        }

        ResourceDTO resource = resourceManager.createResource(resourceName, resourceType, parentId, null, sort);
        resourcePermissionManager.createResourcePermissions(userManager.getCurrentUser().getId(), resource.getId(),
                ResourcePermissionTypeEnum.getResourcePermissionTypeEnumListWhenCreation(resourceType));
        return resource;
    }

    @Override
    @Transactional
    public ResourceDTO modifyResource(String resourceId, String resourceName) {
        ResourceDTO resource = resourceManager.getResource(resourceId);
        ResourceTypeEnum resourceType = resource.getResourceType();
        ResourcePermissionTypeEnum updateResourcePermissionType;

        switch (resourceType){
            case BOOK:
                updateResourcePermissionType = ResourcePermissionTypeEnum.UPDATE_BOOK;
                break;
            case PORTFOLIO:
                updateResourcePermissionType = ResourcePermissionTypeEnum.UPDATE_PORTFOLIO;
                break;
            case APPROVAL_GROUP_INFO:
                updateResourcePermissionType = ResourcePermissionTypeEnum.UPDATE_APPROVAL_GROUP;
                break;
            case TRIGGER_INFO:
                updateResourcePermissionType = ResourcePermissionTypeEnum.UPDATE_TRIGGER;
                break;
            default:
                updateResourcePermissionType = ResourcePermissionTypeEnum.UPDATE_NAMESPACE;
        }

        if (!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceId, updateResourcePermissionType)) {
            throw new AuthorizationException(resourceType, resource.getResourceName(), updateResourcePermissionType);
        }

        return resourceManager.updateResourceNameByResourceId(resourceId, resourceName);
    }

    @Override
    @Transactional
    public Resource moveResource(String resourceId, String parentId) {
        ResourceDTO resource = resourceManager.getResource(resourceId);

        if (Objects.equals(resource.getParentId(), parentId)) {
            return resourceManager.getResourceTree();
        }

        ResourcePermissionTypeEnum createResourcePermissionType;
        ResourcePermissionTypeEnum deleteResourcePermissionType;

        switch (resource.getResourceType()){
            case BOOK:
                deleteResourcePermissionType = ResourcePermissionTypeEnum.DELETE_BOOK;
                createResourcePermissionType = ResourcePermissionTypeEnum.CREATE_BOOK;
                break;
            case PORTFOLIO:
                deleteResourcePermissionType = ResourcePermissionTypeEnum.DELETE_PORTFOLIO;
                createResourcePermissionType = ResourcePermissionTypeEnum.CREATE_PORTFOLIO;
                break;
            default:
                deleteResourcePermissionType = ResourcePermissionTypeEnum.DELETE_NAMESPACE;
                createResourcePermissionType = ResourcePermissionTypeEnum.CREATE_NAMESPACE;
        }

        if (!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(resourceId, deleteResourcePermissionType)) {
            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), deleteResourcePermissionType);
        }

        if (!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(parentId, createResourcePermissionType)) {
            ResourceDTO parentResource = resourceManager.getResource(parentId);
            throw new AuthorizationException(parentResource.getResourceType(), parentResource.getResourceName(), createResourcePermissionType);
        }

        return resourceManager.moveResource(resourceId, parentId);
    }

    @Override
    @Transactional
    public Collection<String> listResourceNameByResourceType(ResourceTypeEnum resourceType) {
        return resourceManager.listResourceByResourceType(resourceType)
                .stream()
                .filter(resourceDto -> resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(
                        resourceDto.getId(),
                        Objects.equals(resourceType, BOOK)
                            ? ResourcePermissionTypeEnum.READ_BOOK
                            : ResourcePermissionTypeEnum.READ_PORTFOLIO
                        ))
                .map(ResourceDTO::getResourceName)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public DepartmentWithResourceDTO getDepartmentResourceByCurrentUser() {
        UserDTO userDto = userManager.getCurrentUser();
        return departmentManager.getDepartmentWithResource(userDto.getDepartmentId());
    }

    @Override
    public Collection<ResourceDTO> getReadableBook() {
        String username =  BeanUtil.getBean(UserInfo.class).getUserName();
        String userId = userManager.getUserByUserName(username).getId();
        return resourceManager.listAllValidResource().stream()
                .filter(r -> Objects.equals(r.getResourceType(), ResourceTypeEnum.BOOK))
                .collect(Collectors.toSet())
                .stream()
                .filter(r -> resourcePermissionAuthAction.hasPermission(userId, r.getId(), ResourcePermissionTypeEnum.READ_BOOK))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<ResourceDTO> getReadableBookAndCanReadTrade() {
        String username =  BeanUtil.getBean(UserInfo.class).getUserName();
        String userId = userManager.getUserByUserName(username).getId();
        return resourceManager.listAllValidResource().stream()
                .filter(r -> Objects.equals(r.getResourceType(), ResourceTypeEnum.BOOK))
                .collect(Collectors.toSet())
                .stream()
                .filter(r -> resourcePermissionAuthAction.hasPermission(userId, r.getId(), ResourcePermissionTypeEnum.READ_BOOK)
                && resourcePermissionAuthAction.hasPermission(userId, r.getId(), ResourcePermissionTypeEnum.READ_TRADE))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<ResourceDTO> getResourceAllByType(ResourceTypeEnum resourceType){
        return resourceManager.listAllValidResource().stream()
                .filter(r -> Objects.equals(r.getResourceType(), resourceType))
                .collect(Collectors.toSet());
    }
}

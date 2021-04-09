package tech.tongyu.bct.auth.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.ResourcePermissionRepo;
import tech.tongyu.bct.auth.dao.ResourceRepo;
import tech.tongyu.bct.auth.dao.RoleResourcePermissionRepo;
import tech.tongyu.bct.auth.dao.entity.ResourceDbo;
import tech.tongyu.bct.auth.dao.entity.ResourcePermissionDbo;
import tech.tongyu.bct.auth.dao.entity.RoleResourcePermissionDbo;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.exception.manager.ManagerException;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.tree.TreeEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResourceManager {

    private ResourceRepo resourceRepo;
    private UserManager userManager;
    private ResourcePermissionRepo resourcePermissionRepo;
    private RoleResourcePermissionRepo roleResourcePermissionRepo;

    @Autowired
    public ResourceManager(
            ResourceRepo resourceRepo
            , UserManager userManager
            , RoleResourcePermissionRepo roleResourcePermissionRepo
            , ResourcePermissionRepo resourcePermissionRepo){
        this.roleResourcePermissionRepo = roleResourcePermissionRepo;
        this.resourceRepo = resourceRepo;
        this.userManager = userManager;
        this.resourcePermissionRepo = resourcePermissionRepo;
    }

    public ResourceDTO getResource(String resourceId){
        return resourceRepo.findValidResourceById(resourceId)
                .map(ConverterUtils::getResourceDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, resourceId));
    }

    public ResourceDTO getResource(String resourceName, ResourceTypeEnum resourceType){
        Collection<ResourceDbo> resourceDbos = resourceRepo.findValidResourceByResourceNameAndResourceType(resourceName, resourceType);
        if(CollectionUtils.isEmpty(resourceDbos))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, resourceName, "", "");

        if(resourceDbos.size() > 1)
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MULTIPLE_RESOURCE_WITH_SAME_NAME, resourceName);

        return ConverterUtils.getResourceDto(resourceDbos.iterator().next());
    }

    public ResourceDTO getResource(String resourceName, ResourceTypeEnum resourceType, String parentId){
        if(Objects.isNull(parentId))
            return resourceRepo.findValidRootResourceByResourceName(resourceName)
                    .map(ConverterUtils::getResourceDto)
                    .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name()));

        return resourceRepo.findValidResourceByResourceNameAndResourceTypeAndParentId(resourceName, resourceType, parentId)
                .map(ConverterUtils::getResourceDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name()));
    }

    public ResourceDTO getRootResource() {
        return resourceRepo.findValidRootResource().map(ConverterUtils::getResourceDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, "ROOT", "", ""));
    }

    public Boolean isRootResourceExist(){
        return resourceRepo.countValidRootResource() > 0;
    }

    public Boolean isResourceExist(String resourceName, ResourceTypeEnum resourceType, String parentId){
        if(Objects.isNull(parentId))
            return resourceRepo.countValidRootResource(resourceName) > 0;
        return resourceRepo.countValidResourceByResourceNameAndResourceTypeAndParentId(resourceName, resourceType, parentId) > 0;
    }

    public void throwExceptionIfResourceExistByResIdAndResType(String resourceName, String resourceTypeString, String parentId){
        throwExceptionIfResourceExistByResIdAndResType(resourceName, ResourceTypeEnum.of(resourceTypeString), parentId);
    }

    public void throwExceptionIfResourceExistByResIdAndResType(String resourceName, ResourceTypeEnum resourceType, String parentId){
        if(isResourceExist(resourceName, resourceType, parentId))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.DUPLICATE_RESOURCE, resourceName, resourceType.name());
    }

    @Transactional
    public ResourceDTO createDepartmentResource(String resourceName, ResourceTypeEnum resourceType, String parentId, String departmentId, Integer sort){
        throwExceptionIfResourceExistByResIdAndResType(resourceName, resourceType, parentId);
        return resourceRepo.findValidResourceById(parentId)
                .map(parent -> resourceRepo.save(new ResourceDbo(resourceName, resourceType, parentId, departmentId, sort)))
                .map(ConverterUtils::getResourceDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name()));
    }

    @Transactional
    public ResourceDTO createResource(String resourceName, ResourceTypeEnum resourceType, String parentId, String departmentId, Integer sort){
        throwExceptionIfResourceExistByResIdAndResType(resourceName, resourceType, parentId);

        return resourceRepo.findValidResourceById(parentId)
                .map(parent -> resourceRepo.save(new ResourceDbo(resourceName, resourceType, parentId, departmentId, sort)))
                .map(ConverterUtils::getResourceDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name()));
    }

    @Transactional
    public ResourceDTO createRootResource(String resourceName, String departmentId){
        throwExceptionIfResourceExistByResIdAndResType(resourceName, ResourceTypeEnum.ROOT, null);
        return ConverterUtils.getResourceDto(resourceRepo.save(new ResourceDbo(resourceName, ResourceTypeEnum.ROOT, null, departmentId, 0)));
    }

    public void deleteResourceByResourceNameAndResourceType(String resourceName, ResourceTypeEnum resourceType){
        if(!CollectionUtils.contains(Lists.newArrayList(ResourceTypeEnum.BOOK, ResourceTypeEnum.PORTFOLIO), resourceType))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.DELETE_RESOURCE_GROUP_ERROR);

        Collection<ResourceDbo> resourceDbos = resourceRepo.findValidResourceByResourceNameAndResourceType(resourceName, resourceType);
        if(CollectionUtils.isEmpty(resourceDbos))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, resourceName, resourceType.name(), "");

        if(resourceDbos.size() > 1)
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MULTIPLE_RESOURCE_WITH_SAME_NAME);

        ResourceDbo resourceDbo = resourceDbos.iterator().next();
        resourceDbo.setRevoked(true);
        resourceRepo.save(resourceDbo);
    }

    @Transactional
    public void deleteResourceByResourceNameAndResourceType(String resourceName, ResourceTypeEnum resourceType, String parentId){
        if(Objects.equals(resourceType, ResourceTypeEnum.ROOT))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.FORBIDDEN_REVOKE_ROOT_RESOURCE);
        resourceRepo.findValidResourceByResourceNameAndResourceTypeAndParentId(resourceName, resourceType, parentId)
                .map(resource -> {
                    resource.setRevoked(true);
                    return resourceRepo.save(resource);
                })
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name()));
    }

    @Transactional
    public void updateResourceResourceName(String resourceName, ResourceTypeEnum resourceType, String parentId, String newResourceName){
        resourceRepo.findValidResourceByResourceNameAndResourceTypeAndParentId(resourceName, resourceType, parentId)
                .map(resource -> {
                    resource.setResourceName(newResourceName);
                    return resourceRepo.save(resource);
                })
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name()));
    }

    @Transactional
    public Collection<ResourceDTO> listResourceByResourceType(ResourceTypeEnum resourceType){
        return resourceRepo.findValidResourceByResourceType(resourceType)
                .stream()
                .map(ConverterUtils::getResourceDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Collection<ResourceDTO> listResourceByResourceId(Collection<String> resourceId){
        if(CollectionUtils.isEmpty(resourceId))
            return Sets.newHashSet();
        return resourceRepo.findValidResourceById(resourceId)
                .stream()
                .map(ConverterUtils::getResourceDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Collection<ResourceDTO> listAllResource(){
        return resourceRepo.findAll()
                .stream()
                .map(ConverterUtils::getResourceDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Collection<ResourceDTO> listAllValidResource(){
        return resourceRepo.findAllValidResource()
                .stream()
                .map(ConverterUtils::getResourceDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteResourceByResourceId(String resourceId) {
        ResourceDbo resource = resourceRepo.findValidResourceById(resourceId).orElseThrow(
                () -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE_BY_ID, resourceId)
        );
        resource.setRevoked(true);
        resource.setRevokeTime(new Date());
        resource.setUpdateTime(new Date());
        resourceRepo.save(resource);
    }

    @Transactional
    public Resource moveResource(String resourceId, String parentId) {
        ResourceDbo resourceDbo = resourceRepo.findValidResourceById(resourceId).orElseThrow(
                () -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE_BY_ID, resourceId)
        );
        if (StringUtils.isNotBlank(parentId))
            resourceDbo.setParentId(parentId);
        return getResourceTree();
    }

    @Transactional
    public ResourceDTO updateResourceNameByResourceId(String resourceId, String resourceName) {
        ResourceDbo resource = resourceRepo.findValidResourceById(resourceId).orElseThrow(
                () -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE_BY_ID, resourceId)
        );
        if (StringUtils.isNotBlank(resourceName))
            resource.setResourceName(resourceName);
        return ConverterUtils.getResourceDto(resourceRepo.save(resource));
    }

    @Transactional
    public Resource getResourceTreeByRoleId(String roleId) {
        return TreeEntity.fromRecords(
                resourceRepo.findAllValidResource()
                , (resourceDbo, parent) -> {
                    ResourceDbo dbo = (ResourceDbo) resourceDbo;
                    return new Resource(resourceDbo.getId(), resourceDbo.getSort(), parent
                            , dbo.getResourceName(), dbo.getResourceType(), dbo.getDepartmentId()
                            , roleResourcePermissionRepo.findValidRoleResourcePermissionByRoleIdAndResourceId(roleId, dbo.getId())
                            .stream().map(RoleResourcePermissionDbo::getResourcePermissionType).collect(Collectors.toSet())
                    );
                }
        );
    }

    @Transactional
    public Resource getResourceTreeByUserId(String userId) {
        return TreeEntity.fromRecords(
                resourceRepo.findAllValidResource()
                , (resourceDbo, parent) -> {
                    ResourceDbo dbo = (ResourceDbo) resourceDbo;
                    return new Resource(resourceDbo.getId(), resourceDbo.getSort(), parent
                            , dbo.getResourceName(), dbo.getResourceType(), dbo.getDepartmentId()
                            , resourcePermissionRepo.findValidResourcePermissionByResourceIdAndUserId(dbo.getId(), userId)
                            .stream().map(ResourcePermissionDbo::getResourcePermissionType).collect(Collectors.toSet())
                    );
                }
        );
    }

    @Transactional
    public Resource getResourceTree(){
        UserDTO userDto = userManager.getCurrentUser();
        return getResourceTreeByUserId(userDto.getId());
    }

    @Transactional
    public List<ResourceDTO> listResource(List<String> resourceName, ResourceTypeEnum resourceTypeEnum){
        Map<String, ResourceDTO> map = resourceRepo.findValidResourceByResourceNameAndResourceType(resourceName, resourceTypeEnum)
                .stream()
                .map(ConverterUtils::getResourceDto)
                .collect(Collectors.toMap(ResourceDTO::getResourceName, Function.identity()));

        resourceName.stream().filter(r -> !map.containsKey(r)).findAny().ifPresent(name -> {
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, name, "", "");
        });

        return resourceName.stream().map(map::get).collect(Collectors.toList());
    }

    @Transactional
    public void modifyResourceNameByResourceId(String resourceId, String newResourceName){
        resourceRepo.findValidResourceById(resourceId)
                .map(resourceDbo -> {
                    resourceDbo.setResourceName(newResourceName);
                    return resourceRepo.save(resourceDbo);
                })
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, resourceId, "", ""));
    }
}

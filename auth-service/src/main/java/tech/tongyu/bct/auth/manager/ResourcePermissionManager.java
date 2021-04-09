package tech.tongyu.bct.auth.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.auth.dao.ResourcePermissionRepo;
import tech.tongyu.bct.auth.dao.ResourceRepo;
import tech.tongyu.bct.auth.dao.UserRepo;
import tech.tongyu.bct.auth.dao.entity.ResourceDbo;
import tech.tongyu.bct.auth.dao.entity.ResourcePermissionDbo;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.exception.manager.ManagerException;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;
import tech.tongyu.bct.auth.dto.ResourcePermissionDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResourcePermissionManager {

    private ResourcePermissionRepo resourcePermissionRepo;
    private ResourceRepo resourceRepo;
    private UserRepo userRepo;

    @Autowired
    public ResourcePermissionManager(
            ResourcePermissionRepo resourcePermissionRepo
            , ResourceRepo resourceRepo
            , UserRepo userRepo) {
        this.resourcePermissionRepo = resourcePermissionRepo;
        this.resourceRepo = resourceRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Boolean hasPermission(String userId, String resourceId, ResourcePermissionTypeEnum resourcePermissionType) {
        return resourcePermissionRepo.countValidResourcePermissionByResourceIdAndUserIdAndResourcePermissionType(resourceId, userId, resourcePermissionType) > 0;
    }

    @Transactional
    public List<Boolean> hasPermission(String userId, List<String> resourceId, ResourcePermissionTypeEnum resourcePermissionType) {
        if(CollectionUtils.isEmpty(resourceId))
            return Lists.newArrayList();

        if(resourceId.stream().allMatch(Objects::isNull))
            return resourceId.stream().map(obj -> false).collect(Collectors.toList());

        Map<String, ResourcePermissionDTO> map =
                resourcePermissionRepo.findValidResourcePermissionByResourceIdAndUserIdAndResourcePermissionType(
                        resourceId.stream().filter(id -> !Objects.isNull(id)).collect(Collectors.toList())
                        , userId, resourcePermissionType)
                        .stream()
                        .map(ConverterUtils::getResourcePermissionDto)
                        .collect(Collectors.toMap(ResourcePermissionDTO::getResourceId, Function.identity()));

        return resourceId.stream()
                .map(resId -> {
                    if(Objects.isNull(resId)) return false;
                    return !Objects.isNull(map.get(resId));
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Boolean> hasPermission(String userId, String resourceId, List<ResourcePermissionTypeEnum> resourcePermissionTypeList) {
        Collection<ResourcePermissionTypeEnum> resourcePermissionTypes = resourcePermissionRepo.findValidResourcePermissionByResourceIdAndUserId(resourceId, userId)
                .stream()
                .map(ResourcePermissionDbo::getResourcePermissionType)
                .collect(Collectors.toSet());

        return resourcePermissionTypeList
                .stream()
                .map(resourcePermissionTypeEnum -> CollectionUtils.contains(resourcePermissionTypes, resourcePermissionTypeEnum))
                .collect(Collectors.toList());
    }

    @Transactional
    public ResourceDbo getResource(String resourceName, ResourceTypeEnum resourceType, String parentId) {
        Optional<ResourceDbo> resourceOpt;
        if (Objects.isNull(parentId))
            resourceOpt = resourceRepo.findValidRootResourceByResourceName(resourceName);
        else
            resourceOpt = resourceRepo.findValidResourceByResourceNameAndResourceTypeAndParentId(resourceName, resourceType, parentId);
        if (!resourceOpt.isPresent())
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_RESOURCE, parentId, resourceName, resourceType.name());

        return resourceOpt.get();
    }

    @Transactional
    public Collection<ResourcePermissionDTO> createResourcePermissions(String username, String resourceName, ResourceTypeEnum resourceType
            , String parentId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnums) {
        return userRepo.findValidUserByUsername(username)
                .map(userDbo -> {
                    ResourceDbo resourceDbo = getResource(resourceName, resourceType, parentId);
                    return createResourcePermissions(userDbo.getId(), resourceDbo.getId(), resourcePermissionTypeEnums);
                })
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.NO_SUCH_USER, username));
    }

    @Transactional
    public Collection<ResourcePermissionDTO> createResourcePermissions(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnums) {
        if (CollectionUtils.isEmpty(resourcePermissionTypeEnums))
            return resourcePermissionRepo.findValidResourcePermissionByResourceIdAndUserId(resourceId, userId)
                    .stream()
                    .map(ConverterUtils::getResourcePermissionDto)
                    .collect(Collectors.toSet());
        Collection<ResourcePermissionDbo> resourcePermissionDbos = resourcePermissionRepo.findValidResourcePermissionByResourceIdAndUserId(resourceId, userId);
        Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnumsAlreadyExist =
                resourcePermissionDbos
                        .stream()
                        .map(ResourcePermissionDbo::getResourcePermissionType)
                        .collect(Collectors.toList());

        List<ResourcePermissionDbo> resourcePermissionDboWaitingForAdd =
                resourcePermissionTypeEnums
                        .stream()
                        .filter(resourcePermissionTypeEnum -> !CollectionUtils.contains(resourcePermissionTypeEnumsAlreadyExist, resourcePermissionTypeEnum))
                        .map(resourcePermissionTypeEnum -> new ResourcePermissionDbo(userId, resourceId, resourcePermissionTypeEnum))
                        .collect(Collectors.toList());

        return resourcePermissionRepo.saveAll(resourcePermissionDboWaitingForAdd)
                .stream()
                .map(ConverterUtils::getResourcePermissionDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Collection<ResourcePermissionDTO> modifyResourcePermissions(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnums) {
        resourcePermissionRepo.deleteValidResourcePermissionByUserIdAndResourceId(userId, resourceId);
        return createResourcePermissions(userId, resourceId, resourcePermissionTypeEnums);
    }

    @Transactional
    public Collection<ResourcePermissionTypeEnum> listResourcePermissionTypeByUserIdAndResourceId(String userId, String resourceId) {
        return resourcePermissionRepo.findValidResourcePermissionByResourceIdAndUserId(resourceId, userId)
                .stream()
                .map(ResourcePermissionDbo::getResourcePermissionType)
                .collect(Collectors.toSet());
    }

    public Collection<String> listUserIdByResourceIdsAndResourcePermissionType(List<String> resourceIds, ResourcePermissionTypeEnum resourcePermissionTypeEnum) {
        if(CollectionUtils.isEmpty(resourceIds))
            return Sets.newHashSet();
        return resourcePermissionRepo.findValidUserByResourceIdsAndResourceType(resourceIds, resourcePermissionTypeEnum);
    }

    @Transactional
    public Collection<ResourcePermissionDTO> listResourcePermissionByUserIdsAndResourcePermissionType(String userId, ResourcePermissionTypeEnum resourcePermissionType) {
        return resourcePermissionRepo.findValidResourcePermissionByUserIdAndResourcePermissionType(userId, resourcePermissionType)
                .stream()
                .map(ConverterUtils::getResourcePermissionDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Collection<ResourcePermissionDTO> listResourcePermissionByUserId(String userId) {
        return resourcePermissionRepo.findValidResourcePermissionByUserId(userId).stream()
                .map(ConverterUtils::getResourcePermissionDto).collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteResourcePermissions(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnums) {
        if (CollectionUtils.isNotEmpty(resourcePermissionTypeEnums)) {
            resourcePermissionRepo.deleteValidResourcePermissionByUserIdAndResourceIdAndResourcePermissionType(userId, resourceId, resourcePermissionTypeEnums);
        }
    }
}

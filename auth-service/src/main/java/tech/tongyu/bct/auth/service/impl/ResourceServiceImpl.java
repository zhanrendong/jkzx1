package tech.tongyu.bct.auth.service.impl;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.business.intel.ResourceBusiness;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.IntegerUtils;

import java.util.Collection;
import java.util.HashMap;

@Service
public class ResourceServiceImpl implements ResourceService {

    private ResourceAuthAction resourceAuthAction;
    private ResourceBusiness resourceBusiness;

    @Autowired
    public ResourceServiceImpl(
            ResourceBusiness resourceBusiness,
            ResourceAuthAction resourceAuthAction) {
        this.resourceBusiness = resourceBusiness;
        this.resourceAuthAction = resourceAuthAction;
    }

    @Override
    @BctMethodInfo(
            description = "新增资源信息，创建相应资源需要对应权限 如：创建交易簿需要创建交易簿权限(CREATE_BOOK)",
            retName = "resource",
            retDescription = "资源树",
            returnClass = ResourceDTO.class,
            service = "auth-service"
    )
    @Transactional
    public ResourceDTO authResourceCreate(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "资源类型", argClass = ResourceTypeEnum.class) String resourceType,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_NAME, description = "资源名称") String resourceName,
            @BctMethodArg(name = ApiParamConstants.PARENT_ID, description = "父资源ID") String parentId,
            @BctMethodArg(name = ApiParamConstants.SORT, description = "排序") Number sort) {

         CommonUtils.checkBlankParam(new HashMap<String, String>() {{
             put(ApiParamConstants.RESOURCE_TYPE, resourceType);
             put(ApiParamConstants.RESOURCE_NAME, resourceName);
             put(ApiParamConstants.PARENT_ID, parentId);
         }});

        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.of(resourceType);
        return resourceAuthAction.createResource(resourceName, resourceTypeEnum, parentId, IntegerUtils.num2Integer(sort));
    }

    @Override
    @BctMethodInfo(
            description = "获取资源树",
            retName = "resource",
            retDescription = "资源树",
            returnClass = Resource.class,
            service = "auth-service"
    )
    @Transactional
    public Resource authResourceGet(){
        return resourceAuthAction.getResource();
    }

    @Override
    @BctMethodInfo(
            description = "根据用户ID获取资源树",
            retName = "resource",
            retDescription = "资源树",
            returnClass = Resource.class,
            service = "auth-service"
    )
    @Transactional
    public Resource authResourceGetByUserId(
            @BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId
    ) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USER_ID, userId);
        }});

        return resourceAuthAction.getResourceByUserId(userId);
    }

    @Override
    @BctMethodInfo(
            description = "根据角色ID获取资源树",
            retName = "resource",
            retDescription = "资源树",
            returnClass = Resource.class,
            service = "auth-service"
    )
    @Transactional
    public Resource authResourceGetByRoleId(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID") String roleId
    ) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.ROLE_ID, roleId);
        }});

        return resourceAuthAction.getResourceByRoleId(roleId);
    }

    @Override
    @BctMethodInfo(
            description = "删除资源信息，删除相应资源需要对应权限，如：删除交易簿需要删除交易簿权限(DELETE_BOOK)",
            retName = "resource",
            retDescription = "资源树",
            returnClass = Resource.class,
            service = "auth-service"
    )
    @Transactional
    public Resource authResourceRevoke(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_ID, description = "资源ID") String resourceId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_ID, resourceId);
        }});

        return resourceAuthAction.deleteResource(resourceId);
    }

    @Override
    @BctMethodInfo(
            description = "更新资源信息，更新相应资源需要对应权限，如：更新交易簿需要更新交易簿权限(UPDATE_BOOK)",
            retName = "resource",
            retDescription = "资源树",
            returnClass = Resource.class,
            service = "auth-service"
    )
    @Transactional
    public Resource authResourceModify(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_ID, description = "资源ID") String resourceId,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_NAME, description = "资源名称") String resourceName,
            @BctMethodArg(name = ApiParamConstants.PARENT_ID, description = "父资源ID") String parentId){

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_ID, resourceId);
            put(ApiParamConstants.RESOURCE_NAME, resourceName);
            put(ApiParamConstants.PARENT_ID, parentId);
        }});

        return resourceBusiness.modifyResource(resourceId, resourceName, parentId);
    }

    @Override
    @BctMethodInfo(
            description = "根据资源类型展示资源列表，目前只包括交易簿和投资组合",
            retName = "list of resource name",
            retDescription = "资源名称列表",
            service = "auth-service"
    )
    @Transactional
    public Collection<String> authResourceList(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "资源类型", argClass = ResourceTypeEnum.class) String resourceType){
        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_TYPE, resourceType);
        }});

        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.of(resourceType);
        if(!CollectionUtils.contains(Lists.newArrayList(ResourceTypeEnum.BOOK, ResourceTypeEnum.PORTFOLIO), resourceTypeEnum))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.LIST_RESOURCE_GROUP_ERROR);
        return resourceAuthAction.listResourceNameByResourceType(ResourceTypeEnum.of(resourceType));
    }

    @Override
    @BctMethodInfo(
            description = "增加业务资源节点(交易簿 | 投资组合)",
            retName = "resource",
            retDescription = "资源信息",
            returnClass = ResourceDTO.class,
            service = "auth-service"
    )
    @Transactional
    public ResourceDTO authNonGroupResourceAdd(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "资源类型", argClass = ResourceTypeEnum.class) String resourceType,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_NAME, description = "资源名称") String resourceName,
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId){

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_TYPE, resourceType);
            put(ApiParamConstants.RESOURCE_NAME, resourceName);
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
        }});

        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.of(resourceType);
        if(!CollectionUtils.contains(Lists.newArrayList(ResourceTypeEnum.BOOK, ResourceTypeEnum.PORTFOLIO), resourceTypeEnum))
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.LIST_RESOURCE_GROUP_ERROR);

        return resourceAuthAction.createNonGroupResource(resourceName, resourceTypeEnum, departmentId, 0);
    }

    @Override
    @BctMethodInfo(
            description = "删除业务资源节点(交易簿 | 投资组合)",
            retName = "resource",
            retDescription = "资源信息",
            service = "auth-service"
    )
    @Transactional
    public Boolean authNonGroupResourceRevoke(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "资源类型", argClass = ResourceTypeEnum.class) String resourceType,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_NAME, description = "资源名称") String resourceName){

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_TYPE, resourceType);
            put(ApiParamConstants.RESOURCE_NAME, resourceName);
        }});

        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.of(resourceType);
        if(!CollectionUtils.contains(Lists.newArrayList(ResourceTypeEnum.BOOK, ResourceTypeEnum.PORTFOLIO), resourceTypeEnum))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.DELETE_RESOURCE_GROUP_ERROR);
        resourceAuthAction.revokeResource(resourceName, resourceTypeEnum);
        return true;
    }

    @Override
    @BctMethodInfo(
            description = "更新业务资源节点(交易簿 | 投资组合)",
            retName = "resource",
            retDescription = "资源信息",
            service = "auth-service"
    )
    @Transactional
    public Boolean authNonGroupResourceModify(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_NAME, description = "资源名称") String resourceName,
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "资源类型", argClass = ResourceTypeEnum.class) String resourceType,
            @BctMethodArg(name = ApiParamConstants.NEW_RESOURCE_NAME, description = "新资源名称") String newResourceName) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_NAME, resourceName);
            put(ApiParamConstants.RESOURCE_TYPE, resourceType);
            put(ApiParamConstants.NEW_RESOURCE_NAME, newResourceName);
        }});

        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.of(resourceType);
        if(!CollectionUtils.contains(Lists.newArrayList(ResourceTypeEnum.BOOK, ResourceTypeEnum.PORTFOLIO), resourceTypeEnum))
            throw new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MODIFY_RESOURCE_GROUP_ERROR);

        resourceAuthAction.modifyNonGroupResourceName(resourceName, resourceTypeEnum, newResourceName);
        return true;
    }

    @Override
    @BctMethodInfo(
            description = "获取当前用户可读交易簿列表",
            retName = "resource dto",
            retDescription = "资源信息列表",
            returnClass = ResourceDTO.class,
            service = "auth-service"
    )
    public Collection<ResourceDTO> authBookGetCanRead(
    ) {
        return resourceAuthAction.getReadableBook();
    }

    @BctMethodInfo(description = "get all resource the system has")
    public Collection<ResourceDTO> authResourceGetAll(
            @BctMethodArg(name = ApiParamConstants.RESOURCE_TYPE, description = "the type of resource") String resourceType){
        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.RESOURCE_TYPE, resourceType);
        }});
        return resourceAuthAction.getResourceAllByType(ResourceTypeEnum.of(resourceType));
    }

}

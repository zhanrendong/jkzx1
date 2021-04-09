package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.dto.ResourceDTO;

import java.util.Collection;

public interface ResourceService {

    ResourceDTO authResourceCreate(String resourceType, String resourceName, String parentId, Number sort);

    Resource authResourceGet();

    Resource authResourceGetByUserId(String userId);

    Resource authResourceGetByRoleId(String roleId);

    Resource authResourceRevoke(String resourceId);

    Resource authResourceModify(String resourceId, String resourceName, String parentId);

    Collection<String> authResourceList(String resourceType);

    ResourceDTO authNonGroupResourceAdd(String resourceType, String resourceName, String departmentId);

    Boolean authNonGroupResourceRevoke(String resourceType, String resourceName);

    Boolean authNonGroupResourceModify(String resourceName, String resourceType, String newResourceName);

    Collection<ResourceDTO> authBookGetCanRead();

    Collection<ResourceDTO> authResourceGetAll(String resourceType);
}

package tech.tongyu.bct.auth.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.DepartmentAuthAction;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.business.intel.ResourceBusiness;


import tech.tongyu.bct.auth.dto.Department;
import tech.tongyu.bct.auth.dto.Resource;
import tech.tongyu.bct.auth.manager.ResourceManager;
import tech.tongyu.bct.auth.dto.ResourceDTO;

import java.util.Objects;

@Service
public class ResourceBusinessImpl implements ResourceBusiness {

    private ResourceAuthAction resourceAuthAction;
    private DepartmentAuthAction departmentAuthAction;
    private ResourceManager resourceManager;

    @Autowired
    public ResourceBusinessImpl(
            ResourceManager resourceManager,
            DepartmentAuthAction departmentAuthAction,
            ResourceAuthAction resourceAuthAction
    ) {
        this.resourceManager = resourceManager;
        this.resourceAuthAction = resourceAuthAction;
        this.departmentAuthAction = departmentAuthAction;
    }

    @Override
    @Transactional
    public Resource modifyResource(String resourceId, String resourceName, String parentId) {
        ResourceDTO resource = resourceManager.getResource(resourceId);
        String departmentId = resource.getDepartmentId();
        if (Objects.nonNull(departmentId)) {
            Department department = departmentAuthAction.getDepartmentById(departmentId);
            departmentAuthAction.updateDepartment(department.getId(), resourceName, department.getDepartmentType(), department.getDescription());

            String newParentDepartmentId = resourceManager.getResource(parentId).getDepartmentId();
            departmentAuthAction.moveDepartment(departmentId, newParentDepartmentId);
        }

        resourceAuthAction.modifyResource(resourceId, resourceName);
        return resourceAuthAction.moveResource(resourceId, parentId);
    }
}

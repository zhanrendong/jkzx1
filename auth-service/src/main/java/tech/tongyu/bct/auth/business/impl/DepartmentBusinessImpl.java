package tech.tongyu.bct.auth.business.impl;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tech.tongyu.bct.auth.authaction.intel.DepartmentAuthAction;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.business.intel.DepartmentBusiness;
import tech.tongyu.bct.auth.dto.*;

import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.DepartmentManager;
import tech.tongyu.bct.auth.manager.ResourceManager;
import tech.tongyu.bct.auth.manager.ResourcePermissionManager;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Objects;
import java.util.Set;

@Component
public class DepartmentBusinessImpl implements DepartmentBusiness {

    private DepartmentManager departmentManager;
    private ResourceManager resourceManager;
    private DepartmentAuthAction departmentAuthAction;
    private ResourceAuthAction resourceAuthAction;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private ResourcePermissionManager resourcePermissionManager;
    private UserManager userManager;

    @Autowired
    public DepartmentBusinessImpl(
            DepartmentManager departmentManager
            , ResourceManager resourceManager
            , DepartmentAuthAction departmentAuthAction
            , ResourceAuthAction resourceAuthAction
            , ResourcePermissionAuthAction resourcePermissionAuthAction
            , ResourcePermissionManager resourcePermissionManager
            , UserManager userManager){

        this.departmentManager = departmentManager;
        this.resourceManager = resourceManager;
        this.departmentAuthAction = departmentAuthAction;
        this.resourceAuthAction = resourceAuthAction;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
        this.resourcePermissionManager = resourcePermissionManager;
        this.userManager = userManager;
    }

    @Override
    @Transactional
    public Department createDepartment(String departmentName, String departmentType, String description, String parentId, Integer sort) {

        DepartmentDTO departmentDto = departmentAuthAction.createDepartment(departmentName, departmentType, description, parentId, sort);
        DepartmentWithResourceDTO parentDepartmentWithResourceDTO = departmentManager.getDepartmentWithResource(parentId);
        ResourceDTO resourceDto = resourceAuthAction.createResource(departmentName,
                ResourceTypeEnum.NAMESPACE, parentDepartmentWithResourceDTO.getResourceId(), sort);
        departmentManager.linkDepartmentAndResource(departmentDto.getId(), resourceDto.getId());
        resourcePermissionManager.createResourcePermissions(userManager.getCurrentUser().getId(),
                resourceDto.getId(), ResourcePermissionTypeEnum.Arrays.WHEN_CREATE_DEPARTMENT);
        return departmentManager.getCompanyDepartment();
    }

    @Override
    @Transactional
    public void deleteDepartment(String departmentId) {

        DepartmentWithResourceDTO departmentWithResource = departmentManager.getDepartmentWithResource(departmentId);
        if (Objects.isNull(departmentWithResource.getParentId()))
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.DELETE_COMPANY_NOT_PERMITTED);

        if (!resourcePermissionAuthAction.hasDepartmentResourcePermissionForCurrentUser(departmentWithResource.getParentId(), ResourcePermissionTypeEnum.DELETE_DEPARTMENT)) {
            String departmentName = departmentManager.getDepartmentDTO(departmentWithResource.getParentId()).getDepartmentName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, departmentName, ResourcePermissionTypeEnum.DELETE_DEPARTMENT);
        }

        if (!resourcePermissionAuthAction.hasResourcePermissionForCurrentUser(departmentWithResource.getResourceId(), ResourcePermissionTypeEnum.DELETE_NAMESPACE)) {
            ResourceDTO resource = resourceManager.getResource(departmentWithResource.getResourceId());
            throw new AuthorizationException(resource.getResourceType(), resource.getResourceName(), ResourcePermissionTypeEnum.DELETE_NAMESPACE);
        }

        Set<Department> children = departmentAuthAction.getDepartmentById(departmentId).getChildren();
        if (Objects.nonNull(children) && children.size() > 0)
            throw new AuthServiceException(
                    ReturnMessageAndTemplateDef.Errors.DELETE_NONE_EMPTY_DEPARTMENT, departmentWithResource.getDepartmentName());

        userManager.listUserByDepartmentId(Lists.newArrayList(departmentId)).stream().map(UserDTO::getId).forEach(userManager::revokeUser);
        departmentManager.deleteDepartment(departmentId);
        resourceManager.deleteResourceByResourceId(departmentWithResource.getResourceId());
    }

    @Override
    @Transactional
    public Department getDepartment() {
        return departmentManager.getCompanyDepartment();
    }

    @Override
    @Transactional
    public Department getDepartment(String departmentId) {
        return departmentAuthAction.getDepartmentById(departmentId);
    }

    @Override
    @Transactional
    public Department updateDepartment(String departmentId, String departmentName, String departmentType, String description) {
        return departmentAuthAction.updateDepartment(departmentId, departmentName, departmentType, description);
    }

    @Override
    @Transactional
    public Department moveDepartment(String departmentId, String parentId) {
        DepartmentWithResourceDTO departmentWithResource = departmentManager.getDepartmentWithResource(departmentId);
        DepartmentWithResourceDTO parentDepartmentWithResource = departmentManager.getDepartmentWithResource(parentId);
        if(CollectionUtils.contains(departmentManager.getDepartment(departmentId).getAllChildrenDepartmentId(), parentId)){
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.CAN_NOT_MOVE_TO_CHILDREN_NODE);
        }
        resourceAuthAction.moveResource(departmentWithResource.getResourceId(), parentDepartmentWithResource.getResourceId());
        return departmentAuthAction.moveDepartment(departmentId, parentId);
    }

    @Override
    @Transactional
    public CompanyInfo createCompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description) {
        String companyId = departmentManager.getCompanyDepartmentId()
                .orElseThrow(() -> new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT));

        if (!resourcePermissionAuthAction.hasDepartmentResourcePermissionForCurrentUser(companyId, ResourcePermissionTypeEnum.CREATE_DEPARTMENT)) {
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, departmentManager.getCompanyInfo().getCompanyName(), ResourcePermissionTypeEnum.CREATE_DEPARTMENT);
        }

        return departmentManager.createCompanyInfo(companyName, companyType, unifiedSocialCreditCode, legalPerson, contactEmail, description);
    }

    @Override
    @Transactional
    public CompanyInfo updateCompanyInfo(String companyName, String companyType, String unifiedSocialCreditCode, String legalPerson, String contactEmail, String description) {
        String companyId = departmentManager.getCompanyDepartmentId()
                .orElseThrow(() -> new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT));

        if (!resourcePermissionAuthAction.hasDepartmentResourcePermissionForCurrentUser(companyId, ResourcePermissionTypeEnum.UPDATE_DEPARTMENT)) {
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, departmentManager.getCompanyInfo().getCompanyName(), ResourcePermissionTypeEnum.UPDATE_DEPARTMENT);
        }

        return departmentManager.updateCompanyInfo(companyName, companyType, unifiedSocialCreditCode, legalPerson, contactEmail, description);
    }

    @Override
    @Transactional
    public CompanyInfo deleteCompanyInfo() {
        String companyId = departmentManager.getCompanyDepartmentId()
                .orElseThrow(() -> new AuthServiceException( ReturnMessageAndTemplateDef.Errors.MISSING_DEPARTMENT));

        if (!resourcePermissionAuthAction.hasDepartmentResourcePermissionForCurrentUser(companyId, ResourcePermissionTypeEnum.DELETE_DEPARTMENT)) {
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, departmentManager.getCompanyInfo().getCompanyName(), ResourcePermissionTypeEnum.DELETE_DEPARTMENT);
        }

        return departmentManager.deleteCompanyInfo();
    }

    @Override
    @Transactional
    public CompanyInfo getCompanyInfo() {
        return departmentManager.getCompanyInfo();
    }
}

package tech.tongyu.bct.auth.manager.converter;

import org.apache.commons.lang3.time.DateFormatUtils;
import tech.tongyu.bct.auth.dao.entity.*;
import tech.tongyu.bct.auth.dto.*;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConverterUtils {

    public static RoleResourcePermissionDTO getRoleResourcePermissionDto(RoleResourcePermissionDbo roleResourcePermissionDbo){
        return new RoleResourcePermissionDTO(
                roleResourcePermissionDbo.getRoleId()
                , roleResourcePermissionDbo.getResourceId()
                , roleResourcePermissionDbo.getResourcePermissionType()
        );
    }

    public static RoleDTO getRoleDto(RoleDbo roleDbo){
        return new RoleDTO(
                roleDbo.getId()
                , roleDbo.getRoleName()
                , roleDbo.getRemark()
                , roleDbo.getAlias());
    }

    public static UserDTO getUserDto(UserDbo userDbo){
        return new UserDTO(
                userDbo.getId()
                , userDbo.getUsername()
                , userDbo.getNickName()
                , userDbo.getContactEmail()
                , userDbo.getPassword()
                , userDbo.getUserType()
                , userDbo.getLocked()
                , userDbo.getExpired()
                , userDbo.getTimesOfLoginFailure()
                , userDbo.getPasswordExpiredTimestamp()
                , userDbo.getDepartmentDbo().getId()
                , CollectionUtils.isNotEmpty(userDbo.getRoleDbos())
                    ? userDbo.getRoleDbos().stream().map(RoleDbo::getRoleName).collect(Collectors.toSet())
                    : Collections.EMPTY_SET
        );
    }

    public static ResourcePermissionDTO getResourcePermissionDto(String userId, RoleResourcePermissionDTO roleResourcePermissionDto) {
        return new ResourcePermissionDTO(userId, roleResourcePermissionDto.getResourceId(), roleResourcePermissionDto.getResourcePermissionType()
        );
    }

    public static ResourcePermissionDTO getResourcePermissionDto(ResourcePermissionDbo resourcePermissionDbo){
        return new ResourcePermissionDTO(
                resourcePermissionDbo.getUserId()
                , resourcePermissionDbo.getResourceId()
                , resourcePermissionDbo.getResourcePermissionType()
        );
    }

    public static ResourceDTO getResourceDto(ResourceDbo resourceDbo){
        return new ResourceDTO(
                resourceDbo.getId()
                , resourceDbo.getResourceName()
                , resourceDbo.getResourceType()
                , resourceDbo.getParentId()
                , resourceDbo.getDepartmentId()
                , DateFormatUtils.format(resourceDbo.getCreateTime(), "yyyy-MM-dd HH:mm:ss")
        );
    }

    public static UserStatusDTO getUserStatusDTO(UserDTO userDTO){
        return new UserStatusDTO(
                userDTO.getUsername()
                , userDTO.getLocked()
                , userDTO.getExpired()
        );
    }

    public static DepartmentDTO getDepartmentDTO(DepartmentDbo departmentDbo){
        return new DepartmentDTO(
                departmentDbo.getId()
                , departmentDbo.getDepartmentName()
                , departmentDbo.getDepartmentType()
                , departmentDbo.getDescription()
                , departmentDbo.getParentId()
        );
    }

    public static CompanyInfo getCompanyInfo(CompanyDbo companyDbo){
        return new CompanyInfo(
                companyDbo.getCompanyName()
                , companyDbo.getCompanyType()
                , companyDbo.getUnifiedSocialCreditCode()
                , companyDbo.getLegalPerson()
                , companyDbo.getContactEmail()
                , companyDbo.getDescription()
        );
    }

    public static DepartmentWithResourceDTO getDepartmentWithResourceDto(DepartmentDbo departmentDbo){
        ResourceDbo resourceDbo = departmentDbo.getResource();
        if(Objects.isNull(resourceDbo)) {
            return new DepartmentWithResourceDTO(
                    departmentDbo.getId()
                    , departmentDbo.getDepartmentName()
                    , departmentDbo.getDepartmentType()
                    , departmentDbo.getDescription()
                    , departmentDbo.getParentId()
                    ,  null
                    , null
                    , null
                    , null);
        }
        else {
            return new DepartmentWithResourceDTO(
                    departmentDbo.getId()
                    , departmentDbo.getDepartmentName()
                    , departmentDbo.getDepartmentType()
                    , departmentDbo.getDescription()
                    , departmentDbo.getParentId()
                    ,  resourceDbo.getId()
                    , resourceDbo.getResourceName()
                    , resourceDbo.getResourceType()
                    , resourceDbo.getParentId());
        }
    }

    public static PageComponentDTO getPageComponentDTO(PageComponentDbo pageComponentDbo) {
        return new PageComponentDTO(
                pageComponentDbo.getId(),
                pageComponentDbo.getPageName(),
                pageComponentDbo.getSort(),
                pageComponentDbo.getParentId()
        );
    }

    public static PagePermissionDbo getPagePermissionDbo(PagePermissionDTO pagePermissionDTO) {
        return new PagePermissionDbo(pagePermissionDTO.getRoleId(), pagePermissionDTO.getPageComponentId());
    }

    public static PagePermissionDTO getPagePermissionDTO(PagePermissionDbo pagePermissionDbo) {
        return new PagePermissionDTO(pagePermissionDbo.getRoleId(), pagePermissionDbo.getPageComponentId());
    }

    public static PageComponentDbo getPageComponentDbo(PageComponentDTO pageComponentDTO) {

        return new PageComponentDbo(
                pageComponentDTO.getPageName(),
                pageComponentDTO.getSort(),
                pageComponentDTO.getParentId()
        );
    }
}

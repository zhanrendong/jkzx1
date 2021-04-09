package tech.tongyu.bct.auth.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.auth.dao.RoleRepo;
import tech.tongyu.bct.auth.dao.entity.RoleDbo;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.exception.manager.ManagerException;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.auth.dto.RoleDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RoleManager {

    private RoleRepo roleRepo;
    private UserManager userManager;

    @Autowired
    public RoleManager(
            RoleRepo roleRepo,
            UserManager userManager){
        this.roleRepo = roleRepo;
        this.userManager = userManager;
    }

    public Collection<RoleDTO> findRolesByRoleIdList(List<String> roleIdList){
        if(CollectionUtils.isEmpty(roleIdList))
            return Lists.newArrayList();
        return roleRepo.findValidRolesByRoleIds(roleIdList).stream().map(ConverterUtils::getRoleDto).collect(Collectors.toSet());
    }

    public Boolean isRoleExist(String roleName){
        return roleRepo.countValidRoleByRoleName(roleName) > 0;
    }

    public void throwExceptionIfRoleExistByRoleName(String roleName){
        if(isRoleExist(roleName))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.DUPLICATE_ROLE_NAME, roleName);
    }

    @Transactional
    public RoleDTO createRole(String roleName, String alias, String description){
        if(isRoleExist(roleName))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.DUPLICATE_ROLE_NAME, roleName);
        RoleDbo roleDbo = new RoleDbo();
        roleDbo.setRoleName(roleName);
        roleDbo.setAlias(alias);
        roleDbo.setRemark(description);

        return ConverterUtils.getRoleDto(roleRepo.save(roleDbo));
    }

    @Transactional
    public RoleDTO createDefaultRole(String username){
        return createRole(CommonUtils.getDefaultRoleName(username), "default create", "synonym to " + username);
    }

    @Transactional
    public void removeRole(String roleId){
        roleRepo.findValidRoleByRoleId(roleId)
                .map(role -> {
                    role.getUserDbos().forEach(userDbo -> {
                        List<String> roleIds = userDbo.getRoleDbos()
                                .stream()
                                .filter(r -> !Objects.equals(roleId, r.getId()))
                                .map(RoleDbo::getId)
                                .collect(Collectors.toList());
                        userManager.updateUserRoles(userDbo.getId(), roleIds);
                    });
                    if(role.isRevoked())
                        throw new ManagerException(ReturnMessageAndTemplateDef.Errors.REMOVE_REVOKED_ROLE, role.getRoleName());
                    role.setRevoked(true);
                    return roleRepo.save(role);
                })
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.REMOVE_NOT_EXISTED_ROLE, roleId));
    }

    @Transactional
    public RoleDTO updateRole(String roleId, String roleName, String alias, String remark){
        return roleRepo.findValidRoleByRoleId(roleId)
                .map(role -> {
                    if(roleRepo.countValidRoleByRoleNameAndNotRoleId(roleName, roleId) > 0)
                        throw new ManagerException(ReturnMessageAndTemplateDef.Errors.DUPLICATE_ROLE_NAME, roleName);
                    role.setRoleName(roleName);
                    role.setAlias(alias);
                    role.setRemark(remark);
                    return roleRepo.save(role);
                })
                .map(ConverterUtils::getRoleDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.UPDATE_NOT_EXISTED_ROLE, roleId));
    }

    public Collection<RoleDTO> listAllValidRoles(){
        return roleRepo.findAllValidRoles()
                .stream()
                .map(ConverterUtils::getRoleDto)
                .collect(Collectors.toList());
    }

    public RoleDTO getValidRoleWithRoleId(String roleId){
        return roleRepo.findValidRoleByRoleId(roleId)
                .map(ConverterUtils::getRoleDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_ROLE, roleId));
    }

    public RoleDTO getValidRoleWithRoleName(String roleName){
        return roleRepo.findValidRoleByRoleName(roleName)
                .map(ConverterUtils::getRoleDto)
                .orElseThrow(() -> new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_ROLE, roleName));
    }

    public Collection<RoleDTO> listValidRolesByRoleNameList(Collection<String> roleName){
        if(CollectionUtils.isEmpty(roleName))
            return Sets.newHashSet();
        return roleRepo.findValidRolesByRoleName(roleName)
                .stream()
                .map(ConverterUtils::getRoleDto)
                .collect(Collectors.toSet());
    }

    public String getRoleByRoleName(String roleName){
        if(Objects.isNull(roleRepo.findByRoleName(roleName)))
            throw new ManagerException(ReturnMessageAndTemplateDef.Errors.MISSING_ROLE,roleName);
        return roleRepo.findByRoleName(roleName).getId();
    }

    public Collection<UserDTO> getUserListByRoleId(String roleId, Collection<String> departmentIds) {
        return roleRepo.findValidRoleByRoleId(roleId).get().getUserDbos().stream()
                .map(ConverterUtils::getUserDto)
                .filter(r -> departmentIds.contains(r.getDepartmentId()))
                .collect(Collectors.toList());
    }

    public Collection<UserDTO> getUserListByRoleId(String roleId) {
        return roleRepo.findValidRoleByRoleId(roleId).get().getUserDbos().stream()
                .filter(userDbo -> !userDbo.isRevoked())
                .map(ConverterUtils::getUserDto)
                .collect(Collectors.toList());
    }
}

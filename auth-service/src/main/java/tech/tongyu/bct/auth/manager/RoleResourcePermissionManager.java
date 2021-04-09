package tech.tongyu.bct.auth.manager;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.auth.dto.RoleResourcePermissionDTO;
import tech.tongyu.bct.auth.dao.RoleResourcePermissionRepo;
import tech.tongyu.bct.auth.dao.entity.RoleResourcePermissionDbo;
import tech.tongyu.bct.auth.manager.converter.ConverterUtils;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RoleResourcePermissionManager {

    private RoleResourcePermissionRepo roleResourcePermissionRepo;

    @Autowired
    public RoleResourcePermissionManager(
            RoleResourcePermissionRepo roleResourcePermissionRepo){
        this.roleResourcePermissionRepo = roleResourcePermissionRepo;
    }

    public Collection<RoleResourcePermissionDTO> createRoleResourcePermissions(String roleId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypes){
        if(CollectionUtils.isEmpty(resourcePermissionTypes))
            return Sets.newHashSet();
        Collection<RoleResourcePermissionDbo> resourcePermissionDbos = resourcePermissionTypes.stream()
                .map(resourcePermissionType -> new RoleResourcePermissionDbo(roleId, resourceId, resourcePermissionType))
                .collect(Collectors.toSet());

        return roleResourcePermissionRepo.saveAll(resourcePermissionDbos).stream()
                .map(ConverterUtils::getRoleResourcePermissionDto)
                .collect(Collectors.toSet());
    }

    public Collection<RoleResourcePermissionDTO> modifyRoleResourcePermissions(String roleId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypes){
        if(CollectionUtils.isEmpty(resourcePermissionTypes)){
            roleResourcePermissionRepo.deleteValidRoleResourcePermissionByRoleIdAndResourceId(roleId, resourceId);
            return Sets.newHashSet();
        }

        roleResourcePermissionRepo.deleteValidRoleResourcePermissionByRoleIdAndResourceId(roleId, resourceId);
        return createRoleResourcePermissions(roleId, resourceId, resourcePermissionTypes);
    }

    public Collection<RoleResourcePermissionDTO> getRoleResourcePermissions(List<String> roleIds) {
        if (Objects.isNull(roleIds) || roleIds.size() == 0)
            return new HashSet<>();

        return roleResourcePermissionRepo
                .findValidRoleResourcePermissionByRoleId(roleIds.stream().filter(Objects::nonNull).collect(Collectors.toList()))
                .stream().map(ConverterUtils::getRoleResourcePermissionDto).collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public Collection<ResourcePermissionTypeEnum> listResourcePermissionTypeByRoleIdAndResourceId(String roleId, String resourceId) {
        return roleResourcePermissionRepo.findValidRoleResourcePermissionByRoleIdAndResourceId(roleId, resourceId)
                .stream()
                .map(RoleResourcePermissionDbo::getResourcePermissionType)
                .collect(Collectors.toSet());
    }
}

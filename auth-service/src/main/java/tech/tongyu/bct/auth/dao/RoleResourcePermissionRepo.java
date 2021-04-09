package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.entity.RoleResourcePermissionDbo;

import java.util.Collection;
import java.util.List;

public interface RoleResourcePermissionRepo extends JpaRepository<RoleResourcePermissionDbo, String> {

    @Query(value = "select r from RoleResourcePermissionDbo r where r.revoked = false and r.roleId = ?1 and r.resourceId = ?2")
    Collection<RoleResourcePermissionDbo> findValidRoleResourcePermissionByRoleIdAndResourceId(String roleId, String resourceId);

    @Transactional
    @Modifying
    @Query(value = "update RoleResourcePermissionDbo r set r.revoked = true where r.roleId = ?1 and r.resourceId = ?2")
    void deleteValidRoleResourcePermissionByRoleIdAndResourceId(String roleId, String resourceId);

    @Query(value = "select r from RoleResourcePermissionDbo r where r.revoked = false and r.roleId in (?1)")
    Collection<RoleResourcePermissionDbo> findValidRoleResourcePermissionByRoleId(List<String> roleId);

}

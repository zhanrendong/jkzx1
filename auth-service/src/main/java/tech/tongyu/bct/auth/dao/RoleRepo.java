package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.auth.dao.entity.RoleDbo;

import java.util.Collection;
import java.util.Optional;

public interface RoleRepo extends JpaRepository<RoleDbo, String> {

    @Query("from RoleDbo r where r.revoked = false and r.roleName = ?1")
    Optional<RoleDbo> findRoleByRoleName(String roleName);

    @Query(value = "from RoleDbo r where r.revoked=false order by r.createTime asc")
    Collection<RoleDbo> findAllValidRoles();

    @Query(value = "from RoleDbo r where r.revoked = false and r.id in (?1)")
    Collection<RoleDbo> findValidRolesByRoleIds(Collection<String> roleIds);

    @Query("select count(r) from RoleDbo r where r.revoked = false and r.roleName = ?1")
    Integer countValidRoleByRoleName(String roleName);

    @Query("select count(r) from RoleDbo r where r.revoked = false and r.roleName = ?1 and r.id <> ?2")
    Integer countValidRoleByRoleNameAndNotRoleId(String roleName, String roleId);

    @Query("from RoleDbo r where r.revoked = false and r.id = ?1")
    Optional<RoleDbo> findValidRoleByRoleId(String roleId);

    @Query("from RoleDbo r where r.revoked = false and r.roleName = ?1")
    Optional<RoleDbo> findValidRoleByRoleName(String roleName);

    @Query("from RoleDbo r where r.revoked = false and r.roleName in (?1)")
    Collection<RoleDbo> findValidRolesByRoleName(Collection<String> roleName);

    RoleDbo findByRoleName(String roleName);

}

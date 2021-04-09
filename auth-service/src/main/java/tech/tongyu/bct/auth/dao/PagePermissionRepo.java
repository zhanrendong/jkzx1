package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.entity.PagePermissionDbo;

import java.util.Collection;
import java.util.Optional;

public interface PagePermissionRepo extends JpaRepository<PagePermissionDbo, String> {

    @Query("from PagePermissionDbo pc where pc.revoked = false")
    Collection<PagePermissionDbo> findValidPagePermission();

    @Query("from PagePermissionDbo pc where pc.revoked = false and pc.roleId in (?1)")
    Collection<PagePermissionDbo> findValidPagePermissionByRoleIds(Collection<String> roleId);

    @Transactional
    @Modifying
    @Query("update PagePermissionDbo pc set pc.revoked = true where pc.revoked = false and pc.roleId = ?1")
    void deleteValidPagePermissionByRoleId(String roleId);

    @Query("from PagePermissionDbo pc where pc.revoked = false")
    Collection<PagePermissionDbo> findAllValidPagePermission();

    @Query("from PagePermissionDbo pc where pc.revoked = false and pc.roleId = ?1")
    Collection<PagePermissionDbo> findValidPagePermissionByRoleId(String roleId);

    @Query("from PagePermissionDbo pc where pc.revoked = false and pc.roleId = ?1 and pc.pageComponentId = ?2")
    Optional<PagePermissionDbo> findValidPagePermissionByRoleIdAndPageComponentId(String roleId, String pageComponentId);

}

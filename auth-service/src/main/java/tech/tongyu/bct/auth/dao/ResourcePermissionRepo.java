package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.entity.ResourcePermissionDbo;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;

import java.util.Collection;
import java.util.List;

public interface ResourcePermissionRepo extends JpaRepository<ResourcePermissionDbo, String> {

    @Query(value = "from ResourcePermissionDbo r where r.revoked=false and r.userId=?2 and r.resourceId=?1")
    Collection<ResourcePermissionDbo> findValidResourcePermissionByResourceIdAndUserId(String resourceId, String userId);

    @Query(value = "from ResourcePermissionDbo r where r.revoked=false and r.userId=?2 and r.resourceId in ( ?1 ) and r.resourcePermissionType = ?3")
    Collection<ResourcePermissionDbo> findValidResourcePermissionByResourceIdAndUserIdAndResourcePermissionType(Collection<String> resourceId, String userId, ResourcePermissionTypeEnum resourcePermissionType);

    @Query(value = "select count(r) from ResourcePermissionDbo r where r.revoked = false and r.userId = ?2 and r.resourceId = ?1 and r.resourcePermissionType = ?3")
    Integer countValidResourcePermissionByResourceIdAndUserIdAndResourcePermissionType(String resourceId, String userId, ResourcePermissionTypeEnum resourcePermissionTypeEnum);

    @Query(value = "select r from ResourcePermissionDbo r where r.revoked = false and r.userId = ?1 and r.resourcePermissionType = ?2")
    Collection<ResourcePermissionDbo> findValidResourcePermissionByUserIdAndResourcePermissionType(String userId, ResourcePermissionTypeEnum resourcePermissionType);

    @Query(value = "select r from ResourcePermissionDbo  r where r.revoked = false and r.userId = ?1")
    Collection<ResourcePermissionDbo> findValidResourcePermissionByUserId(String userId);

    @Query(value = "select r.userId from ResourcePermissionDbo r where r.revoked = false and r.resourceId in (?1) and r.resourcePermissionType = ?2")
    Collection<String> findValidUserByResourceIdsAndResourceType(List<String> resourceIds, ResourcePermissionTypeEnum resourcePermissionTypeEnum);

    @Modifying
    @Transactional
    @Query(value = "update ResourcePermissionDbo r set r.revoked = true where r.userId = ?1 and r.resourceId = ?2 and r.revoked = false ")
    void deleteValidResourcePermissionByUserIdAndResourceId(String userId, String resourceId);

    @Modifying
    @Transactional
    @Query(value = "update ResourcePermissionDbo r set r.revoked = true where r.userId = ?1 and r.resourceId = ?2 and r.resourcePermissionType in ?3 and r.revoked = false")
    void deleteValidResourcePermissionByUserIdAndResourceIdAndResourcePermissionType(String userId, String resourceId, Collection<ResourcePermissionTypeEnum> resourcePermissionTypeEnums);
}

package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.auth.dao.entity.ResourceDbo;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;

import java.util.Collection;
import java.util.Optional;

public interface ResourceRepo extends JpaRepository<ResourceDbo, String> {

    @Query("select count(r) from ResourceDbo r where r.resourceName = ?1 and r.resourceType = ?2 and r.revoked = false and r.parentId = ?3")
    Integer countValidResourceByResourceNameAndResourceTypeAndParentId(String resourceName, ResourceTypeEnum resourceType, String parentId);

    @Query("select count(r) from ResourceDbo r where r.resourceName = ?1 and r.resourceType = tech.tongyu.bct.auth.enums.ResourceTypeEnum.ROOT and r.parentId is null and r.revoked = false ")
    Integer countValidRootResource(String resourceName);

    @Query("select count(r) from ResourceDbo r where r.resourceType = tech.tongyu.bct.auth.enums.ResourceTypeEnum.ROOT and r.parentId is null and r.revoked = false ")
    Integer countValidRootResource();

    @Query("select r from ResourceDbo r where r.resourceType = ?1 and r.revoked = false ")
    Collection<ResourceDbo> findValidResourceByResourceType(ResourceTypeEnum resourceType);

    @Query("from ResourceDbo r where r.id = ?1 and r.revoked = false ")
    Optional<ResourceDbo> findValidResourceById(String id);

    @Query("from ResourceDbo r where r.id in ( ?1 ) and r.revoked = false ")
    Collection<ResourceDbo> findValidResourceById(Collection<String> resourceId);

    @Query("from ResourceDbo r where r.revoked = false and r.resourceName = ?1 and r.resourceType = ?2 and r.parentId = ?3")
    Optional<ResourceDbo> findValidResourceByResourceNameAndResourceTypeAndParentId(String resourceName, ResourceTypeEnum resourceType, String parentId);

    @Query("from ResourceDbo r where r.revoked = false and r.resourceName = ?1 and r.resourceType = tech.tongyu.bct.auth.enums.ResourceTypeEnum.ROOT and r.parentId is null")
    Optional<ResourceDbo> findValidRootResourceByResourceName(String resourceName);

    @Query("from ResourceDbo r where r.revoked = false and r.resourceType = tech.tongyu.bct.auth.enums.ResourceTypeEnum.ROOT and r.parentId is null")
    Optional<ResourceDbo> findValidRootResource();

    @Query("from ResourceDbo r where r.revoked = false")
    Collection<ResourceDbo> findAllValidResource();

    @Query("from ResourceDbo r where r.revoked = false and r.resourceName = ?1 and r.resourceType = ?2")
    Collection<ResourceDbo> findValidResourceByResourceNameAndResourceType(String resourceName, ResourceTypeEnum resourceType);

    @Query("from ResourceDbo r where r.revoked = false and r.resourceName in ( ?1 ) and r.resourceType = ?2")
    Collection<ResourceDbo> findValidResourceByResourceNameAndResourceType(Collection<String> resourceName, ResourceTypeEnum resourceType);
}
